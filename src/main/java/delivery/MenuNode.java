package delivery;

import java.util.List;
import java.util.Objects;

interface MenuNode { <R> R accept(Visitor<R> visitor); }
interface MenuGroup extends MenuNode { String name(); List<MenuNode> children(); }

record City(String name, List<MenuNode> children) implements MenuGroup {
    City { Objects.requireNonNull(name); children = List.copyOf(children); }
    public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
record Restaurant(String name, List<MenuNode> children) implements MenuGroup {
    Restaurant { Objects.requireNonNull(name); children = List.copyOf(children); }
    public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
record Menu(String name, List<MenuNode> children) implements MenuGroup {
    Menu { Objects.requireNonNull(name); children = List.copyOf(children); }
    public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
record Dish(String name, int calories, String allergen) implements MenuNode {
    Dish { Objects.requireNonNull(name); Objects.requireNonNull(allergen); if (calories < 0) throw new IllegalArgumentException(); }
    public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
record Combo(String name, int calories, String allergen) implements MenuNode {
    Combo { Objects.requireNonNull(name); Objects.requireNonNull(allergen); if (calories < 0) throw new IllegalArgumentException(); }
    public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
record Drink(String name, int calories, String allergen) implements MenuNode {
    Drink { Objects.requireNonNull(name); Objects.requireNonNull(allergen); if (calories < 0) throw new IllegalArgumentException(); }
    public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
record Dessert(String name, int calories, String allergen) implements MenuNode {
    Dessert { Objects.requireNonNull(name); Objects.requireNonNull(allergen); if (calories < 0) throw new IllegalArgumentException(); }
    public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
