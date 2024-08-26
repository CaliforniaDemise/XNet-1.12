package mcjty.xnet.apiimpl.energy;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.xnet.XNet;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.api.helper.AbstractConnectorSettings;
import mcjty.xnet.apiimpl.EnumStringTranslators;
import mcjty.xnet.config.ConfigSetup;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class EnergyConnectorSettings extends AbstractConnectorSettings {

    public static final ResourceLocation iconGuiElements = new ResourceLocation(XNet.MODID, "textures/gui/guielements.png");

    public static final String TAG_MODE = "mode";
    public static final String TAG_RATE = "rate";
    public static final String TAG_MINMAX = "minmax";
    public static final String TAG_PRIORITY = "priority";

    public enum EnergyMode {
        INS,
        EXT
    }

    private EnergyMode energyMode = EnergyMode.INS;

    @Nullable private Integer priority = 0;
    @Nullable private Integer rate = null;
    @Nullable private Integer minmax = null;

    public EnergyConnectorSettings(@Nonnull EnumFacing side) {
        super(side);
    }

    public EnergyMode getEnergyMode() {
        return energyMode;
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        switch (energyMode) {
            case INS:
                return new IndicatorIcon(iconGuiElements, 0, 70, 13, 10);
            case EXT:
                return new IndicatorIcon(iconGuiElements, 13, 70, 13, 10);
        }
        return null;
    }

    @Override
    @Nullable
    public String getIndicator() {
        return null;
    }

    @Override
    public void createGui(IEditorGui gui) {
        advanced = gui.isAdvanced();
        sideGui(gui);
        colorsGui(gui);
        redstoneGui(gui);

        int rfRate = advanced ? ConfigSetup.maxRfRateAdvanced.get() : ConfigSetup.maxRfRateNormal.get();

        gui.nl()
                .choices(TAG_MODE, I18n.format(XNet.MODID + ".energy.mode.tooltip"), energyMode, EnergyMode.values())
                .nl()

                .label(I18n.format(XNet.MODID + ".editor.priority")).integer(TAG_PRIORITY, I18n.format(XNet.MODID + ".editor.priority.tooltip"), priority, 30).nl()

                .label(I18n.format(XNet.MODID + ".editor.rate"))
                .integer(TAG_RATE,
                        (energyMode == EnergyMode.EXT ? I18n.format(XNet.MODID + ".energy.rate.ext.tooltip", rfRate) : I18n.format(XNet.MODID + ".energy.rate.ins.tooltip", rfRate)), rate, 40)
                .shift(10)
                .label(energyMode == EnergyMode.EXT ? I18n.format(XNet.MODID + ".editor.min") : I18n.format(XNet.MODID + ".editor.max"))
                .integer(TAG_MINMAX, energyMode == EnergyMode.EXT ? I18n.format(XNet.MODID + ".energy.minmax.ext.tooltip") : I18n.format(XNet.MODID + ".energy.minmax.ins.tooltip"), minmax, 50);
    }

    private static final Set<String> INSERT_TAGS = ImmutableSet.of(TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3", TAG_RATE, TAG_MINMAX, TAG_PRIORITY);
    private static final Set<String> EXTRACT_TAGS = ImmutableSet.of(TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3", TAG_RATE, TAG_MINMAX, TAG_PRIORITY);

    @Override
    public boolean isEnabled(String tag) {
        if (energyMode == EnergyMode.INS) {
            if (tag.equals(TAG_FACING)) {
                return advanced;
            }
            return INSERT_TAGS.contains(tag);
        } else {
            if (tag.equals(TAG_FACING)) {
                return false;           // We cannot extract from different sides
            }
            return EXTRACT_TAGS.contains(tag);
        }
    }

    @Nonnull
    public Integer getPriority() {
        return priority == null ? 0 : priority;
    }

    @Nullable
    public Integer getRate() {
        return rate;
    }

    @Nullable
    public Integer getMinmax() {
        return minmax;
    }

    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        energyMode = EnergyMode.valueOf(((String)data.get(TAG_MODE)).toUpperCase());
        rate = (Integer) data.get(TAG_RATE);
        minmax = (Integer) data.get(TAG_MINMAX);
        priority = (Integer) data.get(TAG_PRIORITY);
    }

    @Override
    public JsonObject writeToJson() {
        JsonObject object = new JsonObject();
        super.writeToJsonInternal(object);
        setEnumSafe(object, "energymode", energyMode);
        setIntegerSafe(object, "priority", priority);
        setIntegerSafe(object, "rate", rate);
        setIntegerSafe(object, "minmax", minmax);
        if (rate != null && rate > ConfigSetup.maxRfRateNormal.get()) {
            object.add("advancedneeded", new JsonPrimitive(true));
        }
        return object;
    }

    @Override
    public void readFromJson(JsonObject object) {
        super.readFromJsonInternal(object);
        energyMode = getEnumSafe(object, "energymode", EnumStringTranslators::getEnergyMode);
        priority = getIntegerSafe(object, "priority");
        rate = getIntegerSafe(object, "rate");
        minmax = getIntegerSafe(object, "minmax");
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        energyMode = EnergyMode.values()[tag.getByte("itemMode")];
        if (tag.hasKey("priority")) {
            priority = tag.getInteger("priority");
        } else {
            priority = null;
        }
        if (tag.hasKey("rate")) {
            rate = tag.getInteger("rate");
        } else {
            rate = null;
        }
        if (tag.hasKey("minmax")) {
            minmax = tag.getInteger("minmax");
        } else {
            minmax = null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setByte("itemMode", (byte) energyMode.ordinal());
        if (priority != null) {
            tag.setInteger("priority", priority);
        }
        if (rate != null) {
            tag.setInteger("rate", rate);
        }
        if (minmax != null) {
            tag.setInteger("minmax", minmax);
        }
    }
}
