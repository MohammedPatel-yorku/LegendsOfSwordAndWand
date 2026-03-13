const classInfo = {
    ORDER: {
        color: '#6ab0e0',
        desc: '🛡 <strong>Order</strong> — +5 Mana, +2 Defense per level. Can cast <em>Protect</em> (shields party) and <em>Heal</em> (restores health). Masters of balance and restoration.'
    },
    CHAOS: {
        color: '#e06060',
        desc: '🔥 <strong>Chaos</strong> — +3 Attack, +5 Health per level. Wields <em>Fireball</em> (AoE fire) and <em>Chain Lightning</em> (chaining arcane strikes). Unstoppable destructive force.'
    },
    WARRIOR: {
        color: '#b08040',
        desc: '⚔ <strong>Warrior</strong> — +2 Attack, +3 Defense per level. Unleashes <em>Berserker Attack</em> to damage multiple enemies at once. The battlefield\'s iron fist.'
    },
    MAGE: {
        color: '#a070d0',
        desc: '🪄 <strong>Mage</strong> — +5 Mana, +1 Attack per level. Casts <em>Replenish</em> to restore mana to allies. Mysterious, powerful, and unpredictable.'
    }
};

function selectClass(cls) {
    // Clear all selected states
    document.querySelectorAll('.class-card').forEach(c => c.classList.remove('selected'));
    // Mark selected
    document.getElementById('card-' + cls).classList.add('selected');
    // Update detail panel
    const info = classInfo[cls];
    const detail = document.getElementById('classDetail');
    detail.innerHTML = info.desc;
    detail.style.borderLeftColor = info.color;
}