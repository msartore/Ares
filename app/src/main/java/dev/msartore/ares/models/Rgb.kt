package dev.msartore.ares.models

class Rgb {
    var r = 0f
    var g = 0f
    var b = 0f

    fun cssGenerator() = "rgb($r,$g,$b)"
}