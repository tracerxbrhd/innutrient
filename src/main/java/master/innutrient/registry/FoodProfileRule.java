package master.innutrient.registry;

import master.innutrient.nutrition.NutritionProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public record FoodProfileRule(
    Identifier definitionId,
    int entryIndex,
    Identifier item,
    Identifier tag,
    Map<Identifier, Double> nutrients,
    Mode mode,
    int priority,
    boolean disableAutomatic
) {
    public enum Mode { REPLACE, MERGE }

    public FoodProfileRule {
        if ((item == null) == (tag == null)) throw new IllegalArgumentException("exactly one of item or tag is required");
        nutrients = NutritionProfile.normalize(nutrients);
    }

    public boolean matches(ItemStack stack) {
        if (item != null) return item.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        return stack.is(TagKey.create(Registries.ITEM, tag));
    }

    public int specificity() {
        return item == null ? 0 : 1;
    }
}
