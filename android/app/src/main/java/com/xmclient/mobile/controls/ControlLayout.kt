package com.xmclient.mobile.controls

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID

data class ControlLayout(
    val name: String = "XM Default",
    val buttons: MutableList<VirtualButton> = mutableListOf()
) {
    companion object {
        private val gson = Gson()

        fun createDefaultLayout(): ControlLayout {
            val layout = ControlLayout("XM Signature Layout")
            val list = layout.buttons

            // Left Movement / D-PAD
            list.add(VirtualButton("btn_w", "W", 0.08f, 0.60f, 64f, 64f, KeyCodes.KEY_W))
            list.add(VirtualButton("btn_a", "A", 0.02f, 0.72f, 64f, 64f, KeyCodes.KEY_A))
            list.add(VirtualButton("btn_s", "S", 0.08f, 0.72f, 64f, 64f, KeyCodes.KEY_S))
            list.add(VirtualButton("btn_d", "D", 0.14f, 0.72f, 64f, 64f, KeyCodes.KEY_D))

            // Sprint & Sneak (Toggleable)
            list.add(VirtualButton("btn_sprint", "SPRINT", 0.08f, 0.48f, 68f, 44f, KeyCodes.KEY_LEFT_CONTROL, isToggle = true))
            list.add(VirtualButton("btn_sneak", "SNEAK", 0.08f, 0.86f, 68f, 44f, KeyCodes.KEY_LEFT_SHIFT, isToggle = true))

            // Right Action Controls
            list.add(VirtualButton("btn_jump", "JUMP", 0.88f, 0.72f, 76f, 76f, KeyCodes.KEY_SPACE, isRound = true))
            list.add(VirtualButton("btn_attack", "ATTACK", 0.78f, 0.60f, 80f, 80f, KeyCodes.MOUSE_LEFT, isRound = true))
            list.add(VirtualButton("btn_use", "USE", 0.88f, 0.46f, 76f, 76f, KeyCodes.MOUSE_RIGHT, isRound = true))

            // Utilities & XM Shortcuts
            list.add(VirtualButton("btn_inv", "INV", 0.46f, 0.88f, 72f, 46f, KeyCodes.KEY_E))
            list.add(VirtualButton("btn_f3", "F3", 0.02f, 0.04f, 52f, 38f, KeyCodes.KEY_F3))
            list.add(VirtualButton("btn_f5", "F5", 0.10f, 0.04f, 52f, 38f, KeyCodes.KEY_F5))
            list.add(VirtualButton("btn_zoom", "ZOOM", 0.18f, 0.04f, 58f, 38f, KeyCodes.KEY_C))
            list.add(VirtualButton("btn_chat", "CHAT", 0.46f, 0.04f, 64f, 38f, KeyCodes.KEY_T))
            list.add(VirtualButton("btn_pause", "ESC", 0.92f, 0.04f, 52f, 38f, KeyCodes.KEY_ESCAPE))

            return layout
        }

        fun saveToFile(layout: ControlLayout, file: File) {
            val json = gson.toJson(layout)
            file.writeText(json)
        }

        fun loadFromFile(file: File): ControlLayout {
            return if (file.exists()) {
                try {
                    val json = file.readText()
                    gson.fromJson(json, ControlLayout::class.java) ?: createDefaultLayout()
                } catch (_: Exception) {
                    createDefaultLayout()
                }
            } else {
                createDefaultLayout()
            }
        }
    }

    fun addNewButton(label: String, keyCode: Int): VirtualButton {
        val newBtn = VirtualButton(
            id = "btn_custom_${UUID.randomUUID().toString().substring(0, 6)}",
            label = label,
            x = 0.5f,
            y = 0.5f,
            width = 64f,
            height = 50f,
            keyCode = keyCode,
            opacity = 0.85f
        )
        buttons.add(newBtn)
        return newBtn
    }

    fun removeButton(id: String): Boolean {
        return buttons.removeAll { it.id == id }
    }
}
