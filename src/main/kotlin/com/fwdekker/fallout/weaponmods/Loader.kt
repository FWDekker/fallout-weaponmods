package com.fwdekker.fallout.weaponmods

import com.beust.klaxon.Klaxon
import java.io.File
import kotlin.system.exitProcess


data class GameDatabase(
    val looseMods: List<LooseMod>,
    val objectModifiers: List<ObjectModifier>,
    val craftableObjects: List<CraftableObject>,
    val components: List<Component>
) {
    constructor(directory: File) : this(
        Klaxon().parseArray<LooseMod>(File(directory, "misc.json").inputStream())!!,
        Klaxon().parseArray<ObjectModifier>(File(directory, "omod.json").inputStream())!!,
        Klaxon().parseArray<CraftableObject>(File(directory, "cobj.json").inputStream())!!,
        Klaxon().parseArray<Component>(File(directory, "cmpo.json").inputStream())!!
    )
}

data class WeaponMod(
    val esm: ESM?,
    val formIDTemplate: String,
    val weapon: Weapon,
    val effects: String,
    val components: List<Pair<Component, Int>>, // TODO fix component capitalisation
    val value: Int,
    val weight: Double,
    val image: String
) {
    constructor(
        looseMod: LooseMod,
        objectModifier: ObjectModifier,
        craftableObject: CraftableObject,
        database: GameDatabase
    ) :
        this(
            esm = ESM.getESM(looseMod.file),
            formIDTemplate = looseMod.formID.toString(16).let {
                if (it.length > 6) "{{DLC ID|${it.take(6)}}}"
                else "{{ID|$it}}"
            },
            weapon = Weapon.getWeapon(objectModifier.weaponName) ?: Weapon.AlienBlaster, // TODO handle nulls!
            effects = objectModifier.description,
            components = craftableObject.components
                .map { Pair(database.components.single { c -> c.editorID == it.component }, it.count) },
            value = looseMod.value,
            weight = looseMod.weight,
            image = modelToImage(looseMod.model)
        ) {
        require(looseMod.file == objectModifier.file && objectModifier.file == craftableObject.file) { "?" }
        // TODO improve message
    }

    companion object {
        fun create(looseMod: LooseMod, database: GameDatabase): WeaponMod? {
            val objectModifier = database.objectModifiers.singleOrNull { it.looseMod == looseMod.editorID }
            if (objectModifier == null) {
                println("Could not create weapon mod object") // TODO use logger
                return null
            }

            val craftableObject = database.craftableObjects.singleOrNull { it.createdMod == objectModifier.editorID }
            if (craftableObject == null) {
                println("Could not create weapon mod object")
                return null
            }

            return WeaponMod(looseMod, objectModifier, craftableObject, database)
        }
    }
}

data class ESM(
    val fileName: String,
    val name: String,
    val link: String,
    val abbreviation: String,
    val modCategory: String? = null
) {
    fun getWikiLink() =
        if (name == link) "''[[$name]]''"
        else "''[[$link|$name]]''"


    companion object {
        private const val jsonPath = "esms.json"

        fun getESM(fileName: String) =
            Klaxon().parseArray<ESM>(File(jsonPath).inputStream())
                ?.single { it.fileName.toLowerCase() == fileName.toLowerCase() }
    }
}

enum class Weapon(
    val keyword: String,
    val wikiName: String,
    val disambigLink: Boolean
) {
    Pistol10mm("ma_10mm", "10mm pistol", true),
    Pistol44("ma_44", ".44 pistol", false),
    AlienBlaster("ma_alienblaster", "alien blaster pistol", false),
    AssaultRifle("ma_assaultrifle", "assault rifle", true),
    BaseballBat("ma_baseballbat", "baseball bat", true),
    Baton("ma_baton", "baton", false),
    BoxingGlove("ma_boxingglove", "boxing glove", true),
    Broadsider("ma_broadsider", "Broadsider", false),
    ChineseOfficersSword("ma_chineseofficersword", "Chinese officer sword", false),
    CombatShotgun("ma_combatgun", "combat shotgun", true),
    CombatRifle("ma_combatrifle", "combat rifle", false),
    Cryolator("ma_cryolator", "Cryolator", false),
    DeathclawGauntlet("ma_deathclawgauntlet", "deathclaw gauntlet", true),
    Deliverer("ma_deliverer", "Deliverer", false),
    DoubleBarrelShotgun("ma_doublebarrelshotgun", "double-barrel shotgun", true),
    FatMan("ma_fatman", "Fat Man", true),
    Flamer("ma_flamer", "flamer", true),
    GammaGun("ma_gammagun", "gamma gun", false),
    GatlingLaser("ma_gatlinglaser", "Gatling laser", true),
    GaussRifle("ma_gaussrifle", "Gauss rifle", true),
    InstituteLaserGun("ma_institutelasergun", "Institute laser gun", false),
    JunkJet("ma_junkjet", "Junk Jet", false),
    Knife("ma_knife", "combat knife", true),
    LaserGun("ma_lasergun", "laser gun", false),
    LaserMusket("ma_lasermusket", "laser musket", false),
    LeadPipe("ma_leadpipe", "lead pipe", true),
    HuntingRifle("ma_huntingrifle", "hunting rifle", true),
    Machete("ma_machete", "machete", true),
    Minigun("ma_minigun", "minigun", true),
    MissileLauncher("ma_missilelauncher", "missile launcher", true),
    PipeBoltAction("ma_pipeboltaction", "pipe bolt-action", false),
    PipeGun("ma_pipegun", "pipe gun", false),
    PipeRevolver("ma_piperevolver", "pipe revolver", false),
    PipeSyringer("ma_pipesyringer", "Syringer", false),
    PipeWrench("ma_pipewrench", "pipe wrench", true),
    PoolCue("ma_poolcue", "pool cue", true),
    PlasmaGun("ma_plasmagun", "plasma gun", false),
    RailwayRifle("ma_railwayrifle", "railway rifle", true),
    RevolutionarySword("ma_revolutionarysword", "revolutionary sword", false),
    Ripper("ma_ripper", "ripper", true),
    RollingPin("ma_rollingpin", "rolling pin", true),
    Sledgehammer("ma_sledgehammer", "sledgehammer", true),
    SubmachineGun("ma_submachinegun", "submachine gun", true),
    SuperSledge("ma_supersledge", "super sledge", true),
    TireIron("ma_tireiron", "tire iron", true),
    WalkingCane("ma_walkingcane", "walking cane", false),
    AssaultronBlade("dlc01ma_assaultronblade", "assaultron blade", false),
    MrHandyBuzzBlade("dlc01ma_mrhandybuzzblade", "Mr. Handy buzz blade", false),
    LightningGun("dlc01ma_lightninggun", "Tesla rifle", false),
    HarpoonGun("dlc03_ma_harpoongun", "harpoon gun", false),
    LeverGun("dlc03_ma_levergun", "lever-action rifle", true), // TODO link should be (Far Harbor)
    RadiumRifle("dlc03_ma_radiumrifle", "radium rifle", false),
    DisciplesBlade("dlc04_ma_disciplesblade", "Disciples blade", false),
    HandmadeAssaultRifle("dlc04_ma_handmadeassaultrifle", "handmade rifle", false),
    PaddleBall("dlc04_ma_paddleball", "paddle ball", false),
    WesternRevolver("dlc04_ma_revolver", "western revolver", false),
    ThirstZapper("dlc04_ma_thirstzapper", "Thirst Zapper", false);


    fun getWikiLink(uppercase: Boolean) =
        if (disambigLink) "''[[${wikiName.capitalize()}|${if (uppercase) wikiName.capitalize() else wikiName}]]''"
        else "''[[${if (uppercase) wikiName.capitalize() else wikiName}]]''"

    companion object {
        fun getWeapon(keyword: String): Weapon? =
            values().singleOrNull { it.keyword.toLowerCase() == keyword.toLowerCase() }
                .also { if (it == null) println("Unrecognised weapon keyword `$keyword`") }
    }
}

fun modelToImage(model: String): String =
    when (model.toLowerCase()) {
        "props\\modspartbox\\modbox.nif" -> "Fo4 item Mod type A.png"
        "props\\modspartbox\\modcrate.nif" -> "Fo4 item Mod type B.png"
        else -> "".also { println("Unrecognised mod model") }
    }

fun namedAggregation(weaponMods: List<WeaponMod>, property: (WeaponMod) -> String) =
    if (weaponMods.map(property).distinct().size == 1)
        property(weaponMods[0]) // TODO check if empty
    else
        weaponMods.joinToString("<br />") { "${property(it)} (${it.weapon.wikiName})" }

fun main(args: Array<String>) {
    print("Enter JSON location: ")
    val databaseLocation = readLine() ?: exitProcess(-1)
    val database = GameDatabase(File(databaseLocation))

    while (true) {
        print("Enter weapon mod name: ")
        val targetName = readLine() ?: exitProcess(-1)
        launch(database, targetName)
    }
}

private fun launch(database: GameDatabase, modName: String) {
    val looseMods = database.looseMods.asSequence().filter { it.name.toLowerCase().contains(modName) }.toList()
    val weaponMods = looseMods.asSequence()
        .mapNotNull { WeaponMod.create(it, database) }
        .sortedBy { it.weapon.wikiName }
        .toList()
    if (weaponMods.isEmpty()) {
        println("No weapon mods found")
        exitProcess(-1)
    }

    val image =
        weaponMods.groupingBy { it.image }.eachCount().entries.maxBy { it.value }?.key ?: "" // TODO log empty image
    val games = weaponMods.asSequence().mapNotNull { it.esm }.distinct().toList().sortedBy { it.name }
    val appearanceString =
        if (games.size == 1 && games[0] == ESM.getESM("Fallout4.esm"))
            games[0].getWikiLink()
        else if (games.size == 2 && games.contains(ESM.getESM("Fallout4.esm")))
            "${ESM.getESM("Fallout4.esm")!!.getWikiLink()} and its [[Fallout 4 add-ons|add-on]] ${games.asSequence()
                .filterNot { it == ESM.getESM("Fallout4.esm") }
                .first()
                .getWikiLink()
            }"
        else
            "${ESM.getESM("Fallout4.esm")!!.getWikiLink()} and its [[Fallout 4 add-ons|add-ons]] ${games.dropLast(1)
                .asSequence()
                .filterNot { it == ESM.getESM("Fallout4.esm") }
                .map { it.getWikiLink() }
                .joinToString(", ")
            } and ${games.last().getWikiLink()}"
    val ingredients =
        weaponMods.flatMap { mod -> mod.components.map { it.first } }
            .asSequence()
            .distinct()
            .sortedBy { it.name }
            .toList()
    val longestWeaponLink = weaponMods.asSequence()
        .map { it.weapon.getWikiLink(true) }
        .maxBy { it.length }!!
        .length
    val longestIngredientNumber = ingredients
        .map { ing ->
            Pair(
                ing,
                weaponMods
                    .flatMap { it.components }
                    .asSequence()
                    .filter { it.first == ing }
                    .maxBy { it.second }!!
                    .second.toString().length
            )
        }
        .toMap()

    println("""
{{Infobox item
|games        =${games.joinToString(", ") { it.abbreviation }}
|type         =mod
|icon         =
|image        =$image
|effects      =<!-- Variable --> // TODO
|modifies     =${weaponMods.joinToString("<br />") { it.weapon.getWikiLink(true) }}
|value        =${namedAggregation(weaponMods) { it.value.toString() }}
|weight       =${namedAggregation(weaponMods) { it.weight.toString() }}
|baseid       =${namedAggregation(weaponMods) { it.formIDTemplate }}
}}{{Games|${games.joinToString("|") { it.abbreviation }}}}

The '''$modName''' is a [[Fallout 4 weapon mods|weapon mod]] in $appearanceString.

==Effects==
<!-- Variable --> // TODO

==Production==
${
    if (weaponMods.size == 1)
        """
{{Crafting table
${weaponMods[0].components.asSequence().sortedBy { it.first.name }.mapIndexed { index, pair ->
            """
|${"material$index".padEnd(9 + ingredients.size)} =${pair.first.name}
|${"material#$index".padEnd(9 + ingredients.size)} =${pair.second}
""".trimIndent()
        }}
|${"workspace".padEnd(9 + ingredients.size)} =[[Weapons workbench]]
|${"product1".padEnd(9 + ingredients.size)} =$modName
|${"product#1".padEnd(9 + ingredients.size)} =1
}}
        """.trimIndent()
    else
        """
{|class="va-table va-table-center sortable"
!style="width:180px;"| Weapon
${ingredients.joinToString("\n") { "!style=\"width:180px;\"| ${it.name}" }}
${weaponMods.joinToString("") { mod ->
            "|-\n| ${mod.weapon.getWikiLink(true).padEnd(longestWeaponLink)} ${ingredients.asSequence()
                .map { ing -> Pair(ing, mod.components.singleOrNull { comp -> ing == comp.first }?.second ?: 0) }
                .joinToString("") { "|| ${it.second.toString().padStart(longestIngredientNumber[it.first]!!)} " }}\n"
        }}
|}
""".trimIndent()
    }

==Locations==
The $modName can be crafted at any [[weapons workbench]].

{{Navbox weapon mods FO4}}

${games.asSequence()
        .mapNotNull { it.modCategory }
        .joinToString("\n") { "[[Category:$it]]" }}
    """.trimIndent())
}
