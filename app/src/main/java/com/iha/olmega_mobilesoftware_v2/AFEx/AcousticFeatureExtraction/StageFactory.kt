package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

import org.w3c.dom.Element
import java.io.File
import java.lang.reflect.InvocationTargetException
import javax.xml.parsers.DocumentBuilderFactory

/**
 * StageFactory creates a processing tree as defined in an XML-File.
 *
 * Example:
 *
 * <stage feature="StageAudioCapture" id="00">
 * <stage feature="StagePreHighpass" id="10" cutoff_hz="250">
 * <stage feature="StageProcPSD" id="11" blocksize="400" hopsize="200">
 * <stage feature="StageFeatureWrite" id="110" prefix="PSD" nfeatures="1026"></stage>
</stage> *
 * <stage feature="StageProcRMS" id="12" blocksize="400" hopsize="200">
 * <stage feature="StageFeatureWrite" id="120" prefix="RMS" nfeatures="2"></stage>
</stage> *
</stage> *
 * <stage feature="StageAudioWrite" id="30" filename="audio_raw"></stage>
</stage> *
 *
 * For parameter see individual Stages.
 *
 * June 2018, sk
 */
internal class StageFactory {
    /**
     * Parses a stage (i.e. feature extraction / processing) configuration from XML
     * The input argument must specify a valid XML-File.
     *
     *
     * Returns the root stage.
     *
     * @param stageConfig XML-File defining the stage configuration
     * @return stage       Root stage
     */
    fun parseConfig(stageConfig: File?): Stage? {
        var stage: Stage? = null
        try {
            val doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(stageConfig)
            stage = buildStage(doc!!.documentElement)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stage
    }

    /**
     * Crawls along available elements and creates and attaches stages to their respective parent stage.
     *
     *
     * Returns the stage created from parentElement.
     *
     * @param parentElement Current XML element
     * @return parentStage   Stage specified in current element
     */
    fun buildStage(parentElement: Element?): Stage? {

        // get attributes from given element and write as hash map
        val attributes = parentElement!!.attributes
        val parameter = HashMap<Any?, Any?>(attributes!!.length, 1.0f)
        for (i in 0 until attributes.length) {
            val node = attributes.item(i)
            if (node!!.nodeName === "feature") continue
            parameter[node!!.nodeName] = node.nodeValue
        }
        val stageType = Stage::class.java.getPackage().name + "." + parentElement.getAttribute("feature")
        val stageClass: Class<*>?
        var parentStage: Stage? = null
        try {
            println(stageType)

            // Instantiate stage
            stageClass = Class.forName(stageType)
            val constructor = stageClass.getConstructor(HashMap::class.java)
            parentStage = constructor.newInstance(parameter) as Stage
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        val nodes = parentElement.childNodes
        for (i in 0 until nodes!!.length) {
            val node = nodes.item(i) as? Element ?: continue
            try {
                parentStage!!.addConsumer(buildStage(node))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return parentStage
    }

    companion object {
        private val TAG: String? = "StageFactory"
    }
}