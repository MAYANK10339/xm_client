package com.xmclient.mobile.controls

import java.io.Serializable

data class VirtualButton(
    val id: String,
    var label: String,
    var x: Float, // Normalized 0.0 - 1.0 of screen width
    var y: Float, // Normalized 0.0 - 1.0 of screen height
    var width: Float, // In DP or normalized ratio
    var height: Float,
    var keyCode: Int,
    var opacity: Float = 0.85f,
    var isToggle: Boolean = false,
    var isRound: Boolean = false,
    @Transient var isPressed: Boolean = false
) : Serializable

object KeyCodes {
    // GLFW keycodes used by Minecraft Java
    const val KEY_UNKNOWN = 0
    const val KEY_SPACE = 32
    const val KEY_APOSTROPHE = 39
    const val KEY_COMMA = 44
    const val KEY_MINUS = 45
    const val KEY_PERIOD = 46
    const val KEY_SLASH = 47
    
    // Numbers
    const val KEY_0 = 48
    const val KEY_1 = 49
    const val KEY_2 = 50
    const val KEY_3 = 51
    const val KEY_4 = 52
    const val KEY_5 = 53
    const val KEY_6 = 54
    const val KEY_7 = 55
    const val KEY_8 = 56
    const val KEY_9 = 57

    // Alphabet
    const val KEY_A = 65
    const val KEY_B = 66
    const val KEY_C = 67
    const val KEY_D = 68
    const val KEY_E = 69
    const val KEY_F = 70
    const val KEY_G = 71
    const val KEY_H = 72
    const val KEY_I = 73
    const val KEY_J = 74
    const val KEY_K = 75
    const val KEY_L = 76
    const val KEY_M = 77
    const val KEY_N = 78
    const val KEY_O = 79
    const val KEY_P = 80
    const val KEY_Q = 81
    const val KEY_R = 82
    const val KEY_S = 83
    const val KEY_T = 84
    const val KEY_U = 85
    const val KEY_V = 86
    const val KEY_W = 87
    const val KEY_X = 88
    const val KEY_Y = 89
    const val KEY_Z = 90

    // Control & Special
    const val KEY_ESCAPE = 256
    const val KEY_ENTER = 257
    const val KEY_TAB = 258
    const val KEY_BACKSPACE = 259
    const val KEY_LEFT_SHIFT = 340
    const val KEY_LEFT_CONTROL = 341
    const val KEY_LEFT_ALT = 342

    // Function keys
    const val KEY_F1 = 290
    const val KEY_F2 = 291
    const val KEY_F3 = 292
    const val KEY_F5 = 294
    const val KEY_F11 = 300

    // Virtual Mouse Buttons (Mapped in GLFW)
    const val MOUSE_LEFT = 1001 // Attack / Destroy
    const val MOUSE_RIGHT = 1002 // Use / Place
    const val MOUSE_MIDDLE = 1003 // Pick Block

    val KEY_NAME_MAP = mapOf(
        MOUSE_LEFT to "ATTACK (L-Click)",
        MOUSE_RIGHT to "USE (R-Click)",
        MOUSE_MIDDLE to "PICK (M-Click)",
        KEY_SPACE to "JUMP (Space)",
        KEY_LEFT_SHIFT to "SNEAK (Shift)",
        KEY_LEFT_CONTROL to "SPRINT (Ctrl)",
        KEY_E to "INVENTORY (E)",
        KEY_Q to "DROP (Q)",
        KEY_F3 to "DEBUG (F3)",
        KEY_F5 to "CAMERA (F5)",
        KEY_C to "ZOOM (C)",
        KEY_TAB to "PLAYERS (Tab)",
        KEY_T to "CHAT (T)",
        KEY_F to "OFFHAND (F)",
        KEY_ESCAPE to "PAUSE (Esc)",
        KEY_W to "FORWARD (W)",
        KEY_A to "LEFT (A)",
        KEY_S to "BACK (S)",
        KEY_D to "RIGHT (D)"
    )

    fun getKeyName(code: Int): String {
        return KEY_NAME_MAP[code] ?: "KEY #$code"
    }
}
