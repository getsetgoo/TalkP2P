import ServerTalk.Companion.port_B
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import javax.sound.sampled.*
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JSpinner
import kotlin.system.exitProcess

fun main() {
    ServerTalk()
}
class ServerTalk {
    companion object {
        @JvmStatic
        lateinit var connect_B: JButton
        lateinit var disconnect_B: JButton
        lateinit var statusL: JLabel
        lateinit var port_B: JSpinner
        lateinit var port_L: JLabel
        @Throws(IOException::class, LineUnavailableException::class)
        fun connect() {
            val thread: Thread = Send()
            thread.start()
        }

        fun disconnect() {
            exitProcess(0)
        }


    }

    init {
        val frame = JFrame("ServerTalk")
        connect_B = JButton("Connect")
        disconnect_B = JButton("Disconnect")
        disconnect_B.addActionListener { disconnect() }

        statusL = JLabel("")
        port_B = JSpinner()
        port_L = JLabel("PORT ")
        frame.layout = null
        frame.pack()
        frame.setSize(300, 300)
        connect_B.addActionListener {
            try {
                connect()
            } catch (ex: IOException) {
            } catch (ex: LineUnavailableException) {
            }
        }
        statusL.setBounds(20, 10, 250, 30)
        frame.add(statusL)
        port_B.setBounds(100, 40, 140, 30)
        frame.add(port_B)
        port_L.setBounds(60, 40, 100, 30)
        frame.add(port_L)
        connect_B.setBounds(45, 85, 200, 60)
        frame.add(connect_B)
        disconnect_B.setBounds(45, 150, 200, 60)
        frame.add(disconnect_B)


        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
        port_B.value = 8888
    }
}

internal class Send : Thread() {
    override fun run() {
        val port = port_B.value as Int
        val socket: ServerSocket?
        val microphone: DataLine?
        val speakers: SourceDataLine
        val client: Socket?

        try {
            socket = ServerSocket(port)
            ServerTalk.statusL.text = "Connectiong....."
            client = socket.accept()
            ServerTalk.statusL.text = "Connected!"
            val format = AudioFormat(16000f,
                8, 2,
                true, true)
            val info = DataLine.Info(
                TargetDataLine::class.java,
                format
            )

            microphone = AudioSystem.getLine(info) as TargetDataLine
            microphone.open(format)
            microphone.start()
            val `in` = client.getInputStream()

            val dataLineInfo = DataLine.Info(SourceDataLine::class.java, format)
            speakers = AudioSystem.getLine(dataLineInfo) as SourceDataLine
            speakers.open(format)
            speakers.start()
            val bufferForInput = ByteArray(1024)
            var bufferVariableForInput = 0
            val bufferForOutput = ByteArray(1024)
            var bufferVariableForOutput: Int


            while (microphone.read(bufferForOutput, 0, 1024).also { bufferVariableForOutput = it } > 0 || `in`.read(
                    bufferForInput
                ).also { bufferVariableForInput = it } > 0) {
                client.getOutputStream()?.write(bufferForOutput, 0, bufferVariableForOutput)
                speakers.write(bufferForInput, 0, bufferVariableForInput)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: LineUnavailableException) {
            e.printStackTrace()
        }
    }
}