package com.kipti.bnb.foundation.config;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class BnbServerConfig extends ConfigBase {

    public final ConfigBool FLYWHEEL_STORAGE_CAPACITY = this.b(
            false,
            "flywheelBearingStorageCapability",
            "(This block is in development, and disabled in feature flags by default) Determines whether the flywheel bearing should be able to function as kinetic storage, setting this to false this means it will be purely decorative."
    );

    public final ConfigFloat FLYWHEEL_STORAGE_FACTOR = this.f(
            1, 0f, 100f,
            "flywheelStorageFactor",
            "(This block is in development, and disabled in feature flags by default) Multiplier for the kinetic storage capacity of flywheels mounted on flywheel bearings (if enabled). Values higher than one increase capacity. It is not recommended to set the value to 0, as you should disable the storage feature entirely."
    );

    public final ConfigFloat FLYWHEEL_TRANSFER_CAPACITY_PER_ANGULAR_MASS = this.f(
            5, 0.1f, 100f,
            "flywheelTransferCapacityPerAngularMass",
            "Stress units per tick per angular mass the flywheel can transfer. Higher values let a flywheel with a large storage factor fill and drain faster. Default 5 matches pre-config behaviour."
    );

    public final ConfigFloat FLYWHEEL_MAX_RPM_FACTOR = this.f(
            0.25f, 0.1f, 64f,
            "flywheelMaxRpmFactor",
            "Multiplier of Create's maxRotationSpeed (default 256) for the flywheel bearing's maximum RPM when storage is enabled. 0.25 -> 64 RPM (legacy default), 1.0 -> 256 RPM, 4.0 -> 1024 RPM."
    );

    public final ConfigFloat COGWHEEL_CHAIN_DRIVE_COST_FACTOR = this.f(
            1, 0f, 10f,
            "cogwheelChainDriveCostFactor",
            "Multiplier for the number of chains required for a cogwheel chain. Minimum cost is always 1 chain, unless this value is set to 0. Does not affect the number of chains returned by existing chain drives."
    );

    public final ConfigInt HEADLAMP_CC_BLOCK_RANGE = this.i(
            32, 1, 128,
            "headlampCCBlockRange",
            "Maximum range in blocks for the CC peripheral setLamp function. Lamp coordinates must be within [3 - blockRange * 2, blockRange * 2]."
    );

    public final ConfigInt COGWHEEL_MAX_BOUNDS = this.i(
            64, 1, 256,
            "cogwheelMaxBounds",
            "Maximum bounds for cogwheel chain drives. May have undefined behaviour at large values."
    );

    public final ConfigInt COGWHEEL_MAX_NODE_COUNT = this.i(
            64, 1, 256,
            "cogwheelMaxNodeCount",
            "Maximum number of nodes in a cogwheel chain."
    );


    @Override
    public @NotNull String getName() {
        return "server";
    }

}

