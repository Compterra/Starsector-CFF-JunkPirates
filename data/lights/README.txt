LIGHT DATA
id: ProjectileID from the projectile file or WeaponID from the weapon file (for beams).
type: 'projectile' or 'beam'.
size: Size of projectile light.
intensity: Intensity of projectile light.
fadeout: The time it takes for the light to fadeout with the projectile when the projectile begins to fade out. This value should generally mirror the value in the projectile fadeout time.
color: The color of the projectile's light.
offset: The vertical translation down from the centre of the projectile sprite where the light source emits. Often used with missiles.
hit size: Size of the flash when the weapon impacts a combat entity.
hit intensity: The intensity of the light when it hits a combat entity.
hit fadeout: How long it takes for the "hit" light source to fade out after. Should generally match the fadetime in the projectile file.
flash size: Size of the muzzle flash light source when the projectile is created.
flash intensity: Intensity of the muzzle flash light source when the projectile is created.
flash fadeout: Time it takes for the muzzle flash to dissipate.
flash color: The color of the muzzle flash.
flash offset: Vertical translation from centre of sprite. Projectiles appear at the center of the barrel's output location by default; you are unlikely to need to change this.
chance: Probability of a light source being associated with the projectile when it is brought into creation/fired.

TEXTURE DATA
id: HullID or WeaponID from the hull/weapon file.
type: 'ship', 'turret', 'turretbarrel', 'turretunder', 'hardpoint', 'hardpointbarrel', or 'hardpointunder'.
frame: The frame of animation for an animated weapon.
map: 'material' or 'normal'.
category: The category from the "graphics" entry in your mod's settings.json.
key: The key under the given category from the "graphics" entry in your mod's settings.json.