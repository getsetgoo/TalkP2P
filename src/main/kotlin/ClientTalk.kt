import java.io.IOException
import java.net.Socket
import javax.sound.sampled.*
import javax.swing.*

    fun main() {
    ClientTalk()
    }

  class ClientTalk {
    companion object {
        lateinit var disconnect_B: JButton
        lateinit var connection_status: JLabel
        lateinit var jSpinner: JSpinner
        lateinit var jLabel: JLabel
        lateinit var host_Field: JTextField
        lateinit var host_L: JLabel
        @Throws(IOException::class, LineUnavailableException::class)
        fun connect() {
            val thread: Thread = Receive()
            thread.start()
        }

        fun Action_Disconnect() {

            System.exit(0)
        }
    }
    init {
        val frame = JFrame("ClintTalk")
        connect_B = JButton("Connect")
        disconnect_B = JButton("Disconnect")
        disconnect_B.addActionListener { Action_Disconnect() }
        connection_status = JLabel("")
        jSpinner = JSpinner()
        jLabel = JLabel("PORT")
        host_Field = JTextField()
        host_Field.text= "127.0.0.1"
        host_L = JLabel("HOST")

        //adjust size and set layout
        frame.pack()
        frame.setSize(300, 300)
        frame.layout = null

        //action for connect button
        connect_B.addActionListener {
            try {
                connect()
            } catch (ex: IOException) {
            } catch (ex: LineUnavailableException) {
            }
        }

        frame.add(jSpinner)
        jSpinner.setBounds(80, 72, 115, 30)

        frame.add(jLabel)
        jLabel.setBounds(30, 75, 60, 30)

        frame.add(host_Field)
        host_Field.setBounds(80, 40, 115, 30)

        frame.add(host_L)
        host_L.setBounds(30, 40, 80, 30)

        frame.add(connect_B)
        connect_B.setBounds(25, 110, 190, 30)

        frame.add(disconnect_B)
        disconnect_B.setBounds(25, 150, 190, 30)

        frame.add(connection_status)
        connection_status.setBounds(20, 10, 250, 30)

        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
        jSpinner.value = 8888
    }
}

 internal class Receive : Thread() {
    override fun run() {
        val port = ClientTalk.jSpinner.value as Int
        val host = ClientTalk.host_Field.text as String //"127.0.0.1";
        val socket: Socket
        val speakers: SourceDataLine
        val microphone: TargetDataLine?


        try {
            ClientTalk.connection_status.text = "Connecting..."
            socket = Socket(host, port)

            ClientTalk.connection_status.text = "Connected!"

            val `in` = socket.getInputStream()

            val format = AudioFormat(16000f, 8, 2, true, true)

            val data = DataLine.Info(SourceDataLine::class.java, format)
            speakers = AudioSystem.getLine(data) as SourceDataLine
            speakers.open(format)
            speakers.start()

            val out: AutoCloseable?
            out = socket.getOutputStream()

            val info = DataLine.Info(TargetDataLine::class.java, format)
            microphone = AudioSystem.getLine(info) as TargetDataLine
            microphone.open(format)
            microphone.start()
            val bufferForOutput = ByteArray(1024)
            var buffer_Out = 0
            val bufferForInput = ByteArray(1024)
            var Buffer_forInput: Int


            while (`in`.read(bufferForInput)
                    .also { Buffer_forInput = it } > 0 || microphone.read(bufferForOutput, 0, 1024)
                    .also { buffer_Out = it } > 0
            ) {
                out.write(bufferForOutput, 0, buffer_Out)
                speakers.write(bufferForInput, 0, Buffer_forInput)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: LineUnavailableException) {
            e.printStackTrace()
        }
    }
}

lateinit var connect_B: JButton