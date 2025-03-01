package mygame.characters;

public class Zombie extends CharacterBase {
    private int damage;

    public Zombie(String name, int hp, int startX, int startY, int damage) {
        super(name, hp, startX, startY);
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public void update() {
        this.x -= 1;
    }
}
