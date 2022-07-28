package com.iha.olmega_mobilesoftware_v2.AFEx.AcousticFeatureExtraction

import android.content.Context
import android.util.Log
import java.time.Instant
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Abstract class to implement producers (output), consumers (input) and conducers (in- and output).
 * Data is transferred using queues.
 */
abstract class Stage(parameter: HashMap<*, *>) : TreeSet<Any?>() {
    val timeout = 2000 // in ms, wait this long to receive data before stopping a stage.
    var hasInput = true // default mode, use false to bypass receive() & rebuffer()
    var thread: Thread? = null
    private var inQueue: LinkedBlockingQueue<Array<FloatArray>>? = null

    protected fun hasInQueue(): Boolean {
        return inQueue != null
    }

    private val outQueue: MutableSet<LinkedBlockingQueue<Array<FloatArray>>> = HashSet()
    var inStage: Stage? = null
    var consumerSet = ArrayList<Stage?>()

    // params to set via constructor
    var id = 0
    var blockSize = 0
    var hopSize = 0
    var blockSizeOut = 0
    var hopSizeOut = 0

    open fun start() {
        Log.d("Stage", " Starting stage ID $id (input: $hasInput ).")
        val runnable = Runnable { if (hasInput) rebuffer() else process(null) }
        thread = Thread(runnable, "Stage ID $id")
        thread!!.priority = Thread.MAX_PRIORITY
        thread!!.start()

        // call start() of attached consumer
        for (consumer in consumerSet) {
            consumer!!.start()
        }
    }

    open fun stop() {
        thread?.interrupt()
        cleanup()
    }

    open fun rebuffer() {
        var abort = false
        var samples = 0
        val channels = 2
        val buffer = Array<FloatArray>(channels) { FloatArray(blockSize) }
        Log.d(LOG, "$id: Start processing")
        while (!Thread.currentThread().isInterrupted and !abort) {
            var dataIn: Array<FloatArray>?
            dataIn = receive()
            if (dataIn != null) {
                var k = 0
                var m = 0
                try {
                    k = 0
                    while (k < dataIn[0].size) {
                        m = 0
                        while (m < dataIn.size) {
                            buffer[m][samples] = dataIn[m][k]
                            m++
                        }
                        samples++
                        if (samples >= blockSize) {
                            process(buffer)
                            samples = blockSize - hopSize
                            for (i in dataIn.indices) {
                                System.arraycopy(buffer[i], hopSize, buffer[i], 0, blockSize - hopSize)
                            }
                        }
                        k++
                    }
                } catch (e: Exception) {
                    println("<-------------------")
                    println("buffer: " + buffer.size + "|" + buffer[0].size + " dataIn: " + dataIn.size + "|" + dataIn[0].size)
                    println(e.toString())
                    println("---> Line: " + e.stackTrace[0].lineNumber)
                    println("--->$samples | $m | $k")
                    println("ID: $id")
                }
            } else {
                abort = true
            }
        }
        cleanup()
        Log.d(LOG, "$id: Stopped consuming")
    }

    fun receive(): Array<FloatArray>? {
        try {

            //Log.d("Stage", "ID: " + id + " | receive()");inQueue
            return inQueue!!.poll(timeout.toLong(), TimeUnit.MILLISECONDS)

            // rebuffer and call processing here?
        } catch (e: InterruptedException) {
            e.printStackTrace()
            Log.d("Stage", "ID: $id | No elements in queue for $timeout ms. Empty?")
        }
        return null
    }

    fun send(data: Array<FloatArray>) {
        try {
            for (queue in outQueue) {
                queue.put(data.clone())
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun addConsumer(consumer: Stage?) {

        // create new queue for consumer
        val queue = LinkedBlockingQueue<Array<FloatArray>>()

        // set queue in new consumer
        consumer!!.setInQueue(queue)

        // set input/parent stage so we have access to parameters
        // TODO: is accessible another way? Maybe define the whole ArrayList as class variable?
        //  Just passing the stage is simpler, though.
        consumer.inStage = this

        // add queue to output queues of this stage
        outQueue.add(queue)

        // add new consumer to consumers for this stage
        consumerSet.add(consumer)
    }

    fun setInQueue(inQueue: LinkedBlockingQueue<Array<FloatArray>>?) {
        this.inQueue = inQueue
    }

    protected abstract fun process(buffer: Array<FloatArray>?)
    protected open fun cleanup() {}

    companion object {
        val LOG: String = "Stage"
        var context: Context? = null
        var startTime: Instant? = null
        var samplingrate = 0
        var channels = 0
    }

    init {
        if (parameter.isNotEmpty()) {
            id = parameter["id"].toString().toInt()
            Log.d("Stage", " Constructing stage ID $id.")
            blockSize = if (parameter["blocksize"] == null) 400 else parameter["blocksize"].toString().toInt()
            hopSize = if (parameter["hopsize"] == null) blockSize else parameter["hopsize"].toString().toInt()
            blockSizeOut = if (parameter["blockout"] == null) blockSize else parameter["blockout"].toString().toInt()
            hopSizeOut = if (parameter["hopout"] == null) hopSize else parameter["hopout"].toString().toInt()
        }
    }
}