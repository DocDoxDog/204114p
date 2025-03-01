package mygame.characters;

public abstract class CharacterBase {
    protected String name;
    protected int hp;
    protected boolean alive;

    // Optional: position for each character
    protected int x, y;   // coordinates on screen

    public CharacterBase(String name, int hp, int startX, int startY) {
        this.name = name;
        this.hp = hp;
        this.alive = true;
        this.x = startX;
        this.y = startY;
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }

    public void heal(int amount) {
        hp += amount;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public boolean isAlive() {
        return alive;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    // Force subclasses to define "update" logic each frame
    public abstract void update();
}
