package name.betterbeacons.component;

import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.function.UnaryOperator;

public class ModDataComponents {

    // This component stores the list of mineral blocks inside the trinket
    public static final ComponentType<List<ItemStack>> STORED_BLOCKS = register("stored_blocks",
            builder -> builder.codec(ItemStack.CODEC.listOf()));

    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of("betterbeacons", name),
                (builderOperator.apply(ComponentType.builder())).build());
    }

    public static void register() {} // Call this in your ModInitializer
}
