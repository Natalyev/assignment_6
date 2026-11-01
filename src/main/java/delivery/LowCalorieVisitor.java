package delivery;

import java.util.ArrayList;
import java.util.List;

final class LowCalorieVisitor implements Visitor<List<MenuNode>> {
    private final int limit;
    LowCalorieVisitor(int limit) { this.limit = limit; }
    private List<MenuNode> collect(MenuGroup group) {
        List<MenuNode> result = new ArrayList<>();
        group.children().forEach(child -> result.addAll(child.accept(this)));
        return List.copyOf(result);
    }
    private List<MenuNode> match(MenuNode node, int calories) { return calories <= limit ? List.of(node) : List.of(); }
    public List<MenuNode> visit(City node) { return collect(node); }
    public List<MenuNode> visit(Restaurant node) { return collect(node); }
    public List<MenuNode> visit(Menu node) { return collect(node); }
    public List<MenuNode> visit(Dish node) { return match(node, node.calories()); }
    public List<MenuNode> visit(Combo node) { return match(node, node.calories()); }
    public List<MenuNode> visit(Drink node) { return match(node, node.calories()); }
    public List<MenuNode> visit(Dessert node) { return match(node, node.calories()); }
}
