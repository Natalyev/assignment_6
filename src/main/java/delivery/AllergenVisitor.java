package delivery;

import java.util.Set;
import java.util.TreeSet;

final class AllergenVisitor implements Visitor<Set<String>> {
    private final Set<String> found = new TreeSet<>();
    private Set<String> scan(MenuGroup group) {
        group.children().forEach(child -> child.accept(this));
        return Set.copyOf(found);
    }
    private Set<String> add(String allergen) {
        if (!allergen.isBlank()) found.add(allergen);
        return Set.copyOf(found);
    }
    public Set<String> visit(City node) { return scan(node); }
    public Set<String> visit(Restaurant node) { return scan(node); }
    public Set<String> visit(Menu node) { return scan(node); }
    public Set<String> visit(Dish node) { return add(node.allergen()); }
    public Set<String> visit(Combo node) { return add(node.allergen()); }
    public Set<String> visit(Drink node) { return add(node.allergen()); }
    public Set<String> visit(Dessert node) { return add(node.allergen()); }
}
