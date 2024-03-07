package mcjty.xnet.client;

import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import net.minecraft.nbt.CompoundTag;

public class ConnectorInfo {

    private final IChannelType type;
    private final SidedConsumer id;
    private final IConnectorSettings connectorSettings;
    private final boolean advanced;

    public ConnectorInfo(IChannelType type, SidedConsumer id, boolean advanced) {
        this.type = type;
        this.id = id;
        this.advanced = advanced;
        connectorSettings = type.createConnector(id.side().getOpposite());
    }

    public IChannelType getType() {
        return type;
    }

    public boolean isAdvanced() {
        return advanced;
    }

    public IConnectorSettings getConnectorSettings() {
        return connectorSettings;
    }

    public SidedConsumer getId() {
        return id;
    }

    public void writeToNBT(CompoundTag tag) {
        connectorSettings.writeToNBT(tag);
    }

    public void readFromNBT(CompoundTag tag) {
        connectorSettings.readFromNBT(tag);
    }
}
