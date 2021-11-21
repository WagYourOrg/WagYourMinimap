# WagYourMinimap

I have become dis-satisfied with the quality of current minimap offerings. whether that's due to missing functionality,
broken functionality or a general lack of performance, I find that they have become worse over time.

While the quality of this one (at first) won't be as good, I make this with the goal of having an open and easy to work
with api.

## mob icons

the `mobicons.json` config file can be used to specify custom mob icons.
an example of how this looks is:
```json
{
    "minecraft:goat": {
        "texWidth": 64,
        "texHeight": 64,
        "scale": 1,
        "parts": [
            {
                "x": 0,
                "y": 0,
                "w": 10,
                "h": 7,
                "ux": 34,
                "vy": 56,
                "uw": 10,
                "vh": 7
            }
        ]
    }
}
```
any mobs present in this file *should* override the included textures
