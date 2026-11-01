package delivery;

import java.util.ArrayList;
import java.util.List;

final class LeafNameVisitor implements Visitor<List<String>> {
    private List<String> names(MenuGroup group) {
        List<String> names = new ArrayList<>();
        group.children().forEach(child -> names.addAll(child.accept(this)));
        return List.copyOf(names);
    }
    public List<String> visit(City node) { return names(node); }
    public List<String> visit(Restaurant node) { return names(node); }
    public List<String> visit(Menu node) { return names(node); }
    public List<String> visit(Dish node) { return List.of(node.name()); }
    public List<String> visit(Combo node) { return List.of(node.name()); }
    public List<String> visit(Drink node) { return List.of(node.name()); }
    public List<String> visit(Dessert node) { return List.of(node.name()); }
    public List<String> visit(Side node) { return List.of(node.name()); }
}
