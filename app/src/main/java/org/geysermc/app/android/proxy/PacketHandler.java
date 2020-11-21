/*
 * Copyright (c) 2020-2020 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/GeyserAndroid
 */

package org.geysermc.app.android.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackStackPacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePacksInfoPacket;
import com.nukkitx.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;

import java.io.IOException;
import java.security.interfaces.ECPublicKey;

import static org.geysermc.app.android.utils.AndroidUtils.OBJECT_MAPPER;

public class PacketHandler implements BedrockPacketHandler {

    private BedrockServerSession session;
    private ProxyServer masterServer;

    private Player player;

    public PacketHandler(BedrockServerSession session, ProxyServer masterServer) {
        this.session = session;
        this.masterServer = masterServer;

        session.addDisconnectHandler((reason) -> disconnect(reason));
    }

    public void disconnect(DisconnectReason reason) {
        if (player != null) {
            masterServer.getProxyLogger().info(player.getDisplayName() + " 与主服务器断开连接 (" + reason + ")");
            masterServer.getPlayers().remove(player.getXuid());
        }
    }

    @Override
    public boolean handle(LoginPacket packet) {
        // Check the protocol version is correct
        int protocol = packet.getProtocolVersion();
        if (protocol != ProxyServer.CODEC.getProtocolVersion()) {
            PlayStatusPacket status = new PlayStatusPacket();
            if (protocol > ProxyServer.CODEC.getProtocolVersion()) {
                status.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD);
            } else {
                status.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD);
            }
            session.sendPacket(status);
        }

        // Set the session codec
        session.setPacketCodec(ProxyServer.CODEC);

        // Read the raw chain data
        JsonNode rawChainData;
        try {
            rawChainData = OBJECT_MAPPER.readTree(packet.getChainData().toByteArray());
        } catch (IOException e) {
            throw new AssertionError("无法读取链数据!");
        }

        // Get the parsed chain data
        JsonNode chainData = rawChainData.get("chain");
        if (chainData.getNodeType() != JsonNodeType.ARRAY) {
            throw new AssertionError("无效的链数据!");
        }

        try {
            // Parse the signed jws object
            JWSObject jwsObject;
            jwsObject = JWSObject.parse(chainData.get(chainData.size() - 1).asText());

            // Read the JWS payload
            JsonNode payload = OBJECT_MAPPER.readTree(jwsObject.getPayload().toBytes());

            // Check the identityPublicKey is there
            if (payload.get("identityPublicKey").getNodeType() != JsonNodeType.STRING) {
                throw new AssertionError("无效的识别公钥!");
            }

            // Create an ECPublicKey from the identityPublicKey
            ECPublicKey identityPublicKey = EncryptionUtils.generateKey(payload.get("identityPublicKey").textValue());

            // Get the skin data to validate the JWS token
            JWSObject skinData = JWSObject.parse(packet.getSkinData().toString());
            if (skinData.verify(new DefaultJWSVerifierFactory().createJWSVerifier(skinData.getHeader(), identityPublicKey))) {
                // Make sure the client sent over the username, xuid and other info
                if (payload.get("extraData").getNodeType() != JsonNodeType.OBJECT) {
                    throw new AssertionError("缺少客户端数据");
                }

                // Fetch the client data
                JsonNode extraData = payload.get("extraData");

                // Create a new player and add it to the players list
                player = new Player(extraData, session);
                masterServer.getPlayers().put(player.getXuid(), player);

                // Tell the client we have logged in successfully
                PlayStatusPacket playStatusPacket = new PlayStatusPacket();
                playStatusPacket.setStatus(PlayStatusPacket.Status.LOGIN_SUCCESS);
                session.sendPacket(playStatusPacket);

                // Tell the client there are no resourcepacks
                ResourcePacksInfoPacket resourcePacksInfo = new ResourcePacksInfoPacket();
                session.sendPacket(resourcePacksInfo);
            } else {
                throw new AssertionError("无效的识别公钥!");
            }
        } catch (Exception e) {
            // Disconnect the client
            session.disconnect("disconnectionScreen.internalError.cantConnect");
            throw new AssertionError("登录失败", e);
        }

        return false;
    }

    @Override
    public boolean handle(ResourcePackClientResponsePacket packet) {
        switch (packet.getStatus()) {
            case COMPLETED:
                masterServer.getProxyLogger().info("Logged in " + player.getDisplayName() + " (" + player.getXuid() + ", " + player.getIdentity() + ")");
                player.sendStartGame();
                break;
            case HAVE_ALL_PACKS:
                ResourcePackStackPacket stack = new ResourcePackStackPacket();
                stack.setExperimentsPreviouslyToggled(false);
                stack.setForcedToAccept(false);
                stack.setGameVersion("*");
                session.sendPacket(stack);
                break;
            default:
                session.disconnect("disconnectionScreen.resourcePack");
                break;
        }

        return true;
    }

    @Override
    public boolean handle(SetLocalPlayerAsInitializedPacket packet) {
        masterServer.getProxyLogger().debug("Player initialized: " + player.getDisplayName());

        player.connectToServer(ProxyServer.getInstance().getAddress(), ProxyServer.getInstance().getPort());

        return false;
    }
}
