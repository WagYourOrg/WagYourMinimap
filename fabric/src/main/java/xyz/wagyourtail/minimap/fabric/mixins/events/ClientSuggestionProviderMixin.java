package xyz.wagyourtail.minimap.fabric.mixins.events;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

@Mixin(ClientSuggestionProvider.class)
public abstract class ClientSuggestionProviderMixin implements CommandSource, FabricClientCommandSource {
    @Override
    public void sendSystemMessage(Component component) {
        sendFeedback(component);
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

}
