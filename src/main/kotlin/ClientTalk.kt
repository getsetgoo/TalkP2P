import java.io.IOException
import java.net.Socket
import javax.sound.sampled.*
import javax.swing.*

class ClientTalk {
    companion object {
        lateinit var disconnectBtn: JButton
        lateinit var statusLbl: JLabel
        lateinit var portSpinner: JSpinner
        lateinit var portLbl: JLabel
        lateinit var hostField: JTextField
        lateinit var hostLbl: JLabel
        @Throws(IOException::class, LineUnavailableException::class)
        fun connect() {
            val thread: Thread = Receive()
            thread.start()
        }

        fun disconnect() {

            //frame.dispose();
            System.exit(0)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            ClientTalk()
        }
    }

    init {
        val frame = JFrame("TalkNowClint")
        connectBtn = JButton("Connect")
        disconnectBtn = JButton("Disconnect")
        statusLbl = JLabel("")
        portSpinner = JSpinner()
        portLbl = JLabel("Port No.")
        hostField = JTextField()
        hostLbl = JLabel("Host name")

        //adjust size and set layout
        frame.pack()
        frame.setSize(300, 300)
        frame.layout = null

        //action for connect button
        connectBtn.addActionListener {
            try {
                connect()
            } catch (ex: IOException) {
            } catch (ex: LineUnavailableException) {
            }
        }
        //action for disconnect button
        disconnectBtn.addActionListener { disconnect() }

        //add components
        frame.add(connectBtn)
        frame.add(disconnectBtn)
        frame.add(statusLbl)
        frame.add(portSpinner)
        frame.add(portLbl)
        frame.add(hostField)
        frame.add(hostLbl)
        //set component bounds (Absolute Positioning)
        connectBtn.setBounds(25, 110, 170, 30)
        disconnectBtn.setBounds(25, 150, 170, 30)
        statusLbl.setBounds(20, 10, 250, 30)
        portSpinner.setBounds(90, 72, 100, 30)
        portLbl.setBounds(20, 75, 60, 30)
        hostField.setBounds(90, 40, 100, 30)
        hostLbl.setBounds(20, 40, 80, 30)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
        portSpinner.value = null
    }
}

internal class Receive : Thread() {
    override fun run() {
        val port = ClientTalk.portSpinner.value as Int
        val host = ClientTalk.hostField.text as String //"127.0.0.1";
        val socket: Socket
        val speakers: SourceDataLine
        //for sending
        val microphone: TargetDataLine?
        try {
            //socket stuff starts here
            ClientTalk.statusLbl.text = "Connecting..."
            socket = Socket(host, port)
            ClientTalk.statusLbl.text = "Connected!"
            //socket stuff ends here
            //input stream
            val `in` = socket.getInputStream()
            //audioformat
            val format = AudioFormat(16000f, 8, 2, true, true)
            //audioformat
            //selecting and strating speakers
            val dataLineInfo = DataLine.Info(SourceDataLine::class.java, format)
            speakers = AudioSystem.getLine(dataLineInfo) as SourceDataLine
            speakers.open(format)
            speakers.start()

            //for sending
            val out: AutoCloseable?
            out = socket.getOutputStream()

            //selecting and starting microphone
            val info = DataLine.Info(TargetDataLine::class.java, format)
            microphone = AudioSystem.getLine(info) as TargetDataLine
            microphone.open(format)
            microphone.start()
            val bufferForOutput = ByteArray(1024)
            var bufferVariableForOutput = 0
            val bufferForInput = ByteArray(1024)
            var bufferVariableForInput: Int
            while (`in`.read(bufferForInput)
                    .also { bufferVariableForInput = it } > 0 || microphone.read(bufferForOutput, 0, 1024)
                    .also { bufferVariableForOutput = it } > 0
            ) {
                out.write(bufferForOutput, 0, bufferVariableForOutput)
                speakers.write(bufferForInput, 0, bufferVariableForInput)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            //System.out.println("some error occured");
        } catch (e: LineUnavailableException) {
            e.printStackTrace()
        }
    }
}

lateinit var connectBtn: JButton