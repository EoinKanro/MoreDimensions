
## More Dimensions
It's a simple mode that automates creation and deletion dimensions on MC server.

### Explanation
It creates necessary files for new dimensions in `world/datapacks` folder on create command.

And deletes files from `world/dimensions` and `world/datapacks` folders on delete command.

Also, if you want to see changes when creating and deleting, you must restart your server.

When creating a new dimension, the generator can use all available biomes, even from mods, except for end and nether.

### Commands

Admin commands:
- `/moredim create <name> <type>` - create mode dimension
  - type `random` - creates totally random dimension using biomes from mods (could be buggy)
  - type `overworld` - creates dimension using preset `minecraft:overworld`
- `/moredim delete <name>` - delete mode dimension

User commands:
- `/moredim list` - get list of available dimensions for teleportation
- `/moredim tp <name>` - teleport to dimension
- `/moredim tp main/overworld` - teleport to main map

### Possible errors

Error on teleport to random dimension: java.lang.IllegalStateException: Feature order cycle found, involved sources: [Reference{ResourceKey[minecraft:worldgen/biome / twilightforest:dense_mushroom_forest]...

How to solve:
- Restart the server and call `/moredim delete <yourDim>`
- Stop the server
- Create `exclude.json` in `world\datapacks\moredimensions_dimensions`
- Fill the json as array using biomes from the error. For example: `["twilightforest:dense_mushroom_forest","twilightforest:dark_forest_center"]`
- Start the server and try to create new random dimension
- Repeat if necessary

### Additional Resources:
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/

Helper: https://misode.github.io/dimension-type/
        https://misode.github.io/dimension/

