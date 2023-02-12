package talsumi.statuesclassic.client.compat

object SkippedFeatureRenderers {

    private val skipped = HashSet<String>()

    init
    {
        //--------------------------3d Skin Layers--------------------------
        //These behave weirdly with statues, not rotating properly and rendering in full colour even when a statue doesn't, so it's best to skip them.
        //TODO: Find a way to make 3d Skin Layers work with statues
        skipped.add("dev.tr7zw.skinlayers.renderlayers.BodyLayerFeatureRenderer")
        skipped.add("dev.tr7zw.skinlayers.renderlayers.HeadLayerFeatureRenderer")

        //--------------------------Ears--------------------------
        //Issue is on my side, but testing environment is playing up after a driver update so I can't really fix it right now. //TODO: Fix issue with Ears
        skipped.add("com.unascribed.ears.EarsFeatureRenderer")
    }

    fun isSkipped(type: Class<Any>): Boolean
    {
        return skipped.contains(type.canonicalName)
    }
}