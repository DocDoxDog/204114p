package mygame.items;

import mygame.characters.Player;

public abstract class Item {
    protected String itemName;
    protected String imagePath; // Path to the item's icon

    public Item(String itemName, String imagePath) {
        this.itemName = itemName;
        this.imagePath = imagePath;
    }

    public String getItemName() {
        return itemName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public abstract void useItem(Player player);

    // ------------------------------------------------
    // NESTED CLASSES
    // ------------------------------------------------

    /**
     * A public static class for "health" items that restore HP.
     */
    public static class HealthItem extends Item {
        private int healAmount;

        public HealthItem(String name, String imagePath, int healAmount) {
            super(name, imagePath);
            this.healAmount = healAmount;
        }

        @Override
        public void useItem(Player player) {
            // Example method on Player, e.g.: player.heal(healAmount);
            player.heal(healAmount);
        }
    }

    /**
     * A public static class for "damage" items that boost player damage.
     */
    public static class DamageItem extends Item {
        private int damageBoost;

        public DamageItem(String name, String imagePath, int damageBoost) {
            super(name, imagePath);
            this.damageBoost = damageBoost;
        }

        @Override
        public void useItem(Player player) {
            player.setDamage(player.getDamage() + damageBoost);
        }
    }

    // ------------------------------------------------
    // CONCRETE ITEMS
    // ------------------------------------------------

    // // e.g. "Paracetamol"
    // public static class Paracetamol extends HealthItem {
    //     public Paracetamol() {
    //         // itemName, icon path, heal amount
    //         super("Paracetamol", "/assets/paracetamol.jpg", 25);
    //     }
    // }

    public static class MedKit extends HealthItem {
        public MedKit() {
            super("MedKit", "/assets/medkit.jpg", 40);
        }
    }

    public static class EnergyDrink extends HealthItem {
        public EnergyDrink() {
            super("Energy Drink", "/assets/energy_drink.jpg", 15);
        }

        @Override
        public void useItem(Player player) {
            // Heal 15 + boost damage by 10
            player.heal(15);
            player.setDamage(player.getDamage() + 10);
        }
    }

    // public static class AssaultRifleGun extends DamageItem {
    //     public AssaultRifleGun() {
    //         super("Assault Rifle Gun", "/assets/assault_rifle.png", 25);
    //     }
    // }

    // public static class FlareGun extends DamageItem {
    //     public FlareGun() {
    //         super("Flare Gun", "/assets/flare_gun.jpg", 35);
    //     }
    // }

    // public static class LaserGun extends DamageItem {
    //     public LaserGun() {
    //         super("Laser Gun", "/assets/laser_gun.png", 45);
    //     }
    // }
}
