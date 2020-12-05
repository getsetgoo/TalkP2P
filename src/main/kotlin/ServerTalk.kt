import java.io.IOException
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import javax.sound.sampled.*
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JSpinner
import kotlin.system.exitProcess


class ServerTalk {
    companion object {
        @JvmStatic
        lateinit var connectBtn: JButton
        lateinit var disconnectBtn: JButton
        lateinit var statusLbl: JLabel
        lateinit var portSpinner: JSpinner
        lateinit var portLbl: JLabel
        @Throws(IOException::class, LineUnavailableException::class)
        fun connect() {
            val thread: Thread = Send()
            thread.start()
        }

        fun disconnect() {

            //frame.dispose();
            exitProcess(0)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            ServerTalk()
        }
    }

    init {
        val frame = JFrame()
        connectBtn = JButton("Connect")
        disconnectBtn = JButton("Disconnect")
        statusLbl = JLabel("")
        portSpinner = JSpinner()
        portLbl = JLabel("PORT :.")
        //adjust size and set layout
        frame.pack()
        frame.setSize(400, 300)
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
        //set component bounds ( Absolute Positioning)
        connectBtn.setBounds(45, 85, 200, 60)
        disconnectBtn.setBounds(45, 150, 200, 60)
        statusLbl.setBounds(130, 175, 200, 30)
        portSpinner.setBounds(160, 40, 60, 30)
        portLbl.setBounds(100, 40, 60, 30)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
        //set defualts
        portSpinner.value = 8888
    }
}

internal class Send : Thread() {
    override fun run() {
        val port = ServerTalk.portSpinner.value as Int
        val socket: ServerSocket?
        //for sending
        val microphone: DataLine?
        //for recieveing
        val speakers: SourceDataLine
        val client: Socket?

        try {
            //socket stuff starts here
            socket = ServerSocket(port)
            ServerTalk.statusLbl.text = "Waiting for Connection..."
            client = socket.accept()
            ServerTalk.statusLbl.text = "Connected!"
            //socket stuff ends here
            //outputstream
            val out: OutputStream? = client.getOutputStream()
            //outputstream
            //audioformat
            val format = AudioFormat(16000f, 8, 2, true, true)
            //audioformat
            //selecting and starting microphone
            val info = DataLine.Info(
                TargetDataLine::class.java,
                format
            )
            microphone = AudioSystem.getLine(info) as TargetDataLine
            microphone.open(format)
            microphone.start()

            //for recieveing
            val `in` = client.getInputStream()
            //selecting and starting speakers
            val dataLineInfo = DataLine.Info(SourceDataLine::class.java, format)
            speakers = AudioSystem.getLine(dataLineInfo) as SourceDataLine
            speakers.open(format)
            speakers.start()
            val bufferForInput = ByteArray(1024)
            var bufferVariableForInput = 0
            val bufferForOutput = ByteArray(1024)
            var bufferVariableForOutput: Int

            //send/recieve
            while (microphone.read(bufferForOutput, 0, 1024).also { bufferVariableForOutput = it } > 0 || `in`.read(
                    bufferForInput
                ).also { bufferVariableForInput = it } > 0) {
                if (out != null) {
                    out.write(bufferForOutput, 0, bufferVariableForOutput)
                }
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