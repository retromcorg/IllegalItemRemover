package com.johnymuffin.beta.illegalitemremover;

import org.bukkit.inventory.ItemStack;

public class ILFMaterial {
    private final int itemID;
    private final int damageValue;
    private final boolean compareDamage;

    public int getItemID() {
        return itemID;
    }

    public int getDamageValue() {
        return damageValue;
    }

    public boolean isCompareDamage() {
        return compareDamage;
    }

    public ILFMaterial(int itemID, int damageValue, boolean compareDamage) {
        this.itemID = itemID;
        this.damageValue = damageValue;
        this.compareDamage = compareDamage;
    }

    public ILFMaterial(ItemStack itemStack) {
        this.itemID = itemStack.getTypeId();
        if (itemStack.getData() != null) {
            this.damageValue = itemStack.getData().getData();
            compareDamage = true;
        } else {
            this.damageValue = 0;
            this.compareDamage = false;
        }

    }


    public boolean matches(ItemStack itemStack) {
        if (itemStack.getTypeId() == itemID) {
            if (!compareDamage) {
                return true;
            }
            if (itemStack.getData() != null && itemStack.getData().getData() == damageValue) {
                return true;
            }
        }
        return false;
    }


}
