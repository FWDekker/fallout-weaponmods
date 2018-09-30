package com.fwdekker.fallout.weaponmods

import com.beust.klaxon.Klaxon
import java.io.File
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val location = readLine() ?: exitProcess(-1)

    val miscs = Klaxon().parseArray<LooseMod>(File("$location/misc.json").inputStream())
        ?: exitProcess(-1)
    val omods = Klaxon().parseArray<ObjectModifier>(File("$location/omod.json").inputStream())
        ?: exitProcess(-1)
    val cobjs = Klaxon().parseArray<CraftableObject>(File("$location/cobj.json").inputStream())
        ?: exitProcess(-1)

    val targetName = readLine() ?: exitProcess(-1)
    val targetMiscs = miscs.asSequence().filter { it.name.toLowerCase().contains(targetName) }.toList()
    val targetMiscEdids = targetMiscs.map { it.editorID }

    println("Does this look right?")
    println(targetMiscs.joinToString("\n") { it.name })
    if (readLine() != "yes") exitProcess(-1)

    val targetOmods = omods.filter { it.looseMod in targetMiscEdids }
    val targetOmodEdids = targetOmods.map { it.editorID }

    val targetCobjs = cobjs.filter { it.createdMod in targetOmodEdids }

    println("""
        The '''$targetName''' is a [[Fallout 4 weapon mods|weapon mod]] for the ... in ''[[Fallout 4]]''.

         ==Effects==
         ?

         ==Production==
         ?

         ==Locations==
         The $targetName can be crafted at any [[weapons workbench]].
    """.trimIndent())
}
