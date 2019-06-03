package adk.midicontroller;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;

import javax.security.auth.callback.Callback;

/**
 * Created by Andrew on 27/02/2016.
 */
public class MidiDriver {
    private static MidiDriver ourInstance = new MidiDriver();

    Context myContext;

    MidiManager myManager;
    FractalController myController;

    public static MidiDriver getInstance() {
        return ourInstance;
    }

    private MidiDriver() {
    }

    public void init(Context context) {
        this.myContext = context.getApplicationContext();

        this.myManager = ((MidiManager) this.myContext.getSystemService(Context.MIDI_SERVICE));
    }

    public void connect(final Runnable runnable) {
        this.connect(runnable, new Handler());
    }

    public void connect(final Runnable runnable, final Handler handler) {
        MidiDeviceInfo[] info = myManager.getDevices();
        if(info.length > 0) {
            connectToDevice(info[0], runnable, handler);
        }

        this.myManager.registerDeviceCallback(new MidiManager.DeviceCallback() {
            public void onDeviceAdded(MidiDeviceInfo device) {
                connectToDevice(device, runnable, handler);
            }

            public void onDeviceRemoved(MidiDeviceInfo device) {
                // ...
            }
        }, handler);
    }

    private void connectToDevice(MidiDeviceInfo device, final Runnable runnable, final Handler handler) {
        myManager.openDevice(device, new MidiManager.OnDeviceOpenedListener() {
            public void onDeviceOpened(MidiDevice device) {
                MidiController midiController = new MidiController(device);
                myController = new FractalController(midiController);

                handler.post(runnable);
            }
        }, handler);
    }

    public String getDeviceInfo() {
        MidiDeviceInfo[] info = this.myManager.getDevices();

        if(info.length > 0) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < info.length; i++) {
                buffer.append("Device " + i + ": " +
                    info[i].getInputPortCount() + " inputs, " +
                    info[i].getOutputPortCount() + " outputs.\n");
            }
            buffer.append(this.isConnected ? "Connected!" : "Not connected.");
            return buffer.toString();
        } else {
            return "No devices detected";
        }
    }
}
