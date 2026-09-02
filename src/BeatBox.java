import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class BeatBox
{
    JPanel mainPanel;
    JFileChooser fileChooser;
    ArrayList<JCheckBox> checkBoxes;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame frame;
    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", " Acoustic Snare", "Crash Cymbal", "Hand Clap",
            "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main()
    {
        new BeatBox().buildGUI();
    }
    public void buildGUI()
    {
        frame = new JFrame("Cyber BeatBox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout borderLayout = new BorderLayout();
        JPanel background = new JPanel(borderLayout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkBoxes = new ArrayList<>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);


        JButton stop = new JButton("Stop");
        start.addActionListener(new MyStopListener());
        buttonBox.add(stop);


        JButton upTempo = new JButton("Tempo Up");
        start.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);


        JButton downTempo = new JButton("Tempo Down");
        start.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton save = new JButton("Save");
        start.addActionListener(new MySendListener());
        buttonBox.add(save);

        JButton load = new JButton("Load");
        start.addActionListener(new MyReadInListener());
        buttonBox.add(load);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for(int i = 0; i < 16; i++)
        {
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        frame.getContentPane().add(background);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("Load from file");
        menuItem.addActionListener(new MenuListener());
        menu.add(menuItem);
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);


        GridLayout gridLayout = new GridLayout(16, 16);
        gridLayout.setVgap(1);
        gridLayout.setHgap(2);
        mainPanel = new JPanel(gridLayout);
        background.add(BorderLayout.CENTER, mainPanel);
        for(int i = 0; i < 256; i++)
        {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(false);
            checkBoxes.add(checkBox);
            mainPanel.add(checkBox);
        }

        setUpMidi();

        frame.setBounds(50, 50, 300, 300);
        frame.pack();
        frame.setVisible(true);
    }
    public void addTrackAndStart()
    {
        int[] tracklist = null;
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        for(int i = 0; i < 16; i++)
        {
            tracklist = new int[16];

            int key = instruments[i];
            for(int j = 0; j < 16; j++)
            {
                JCheckBox jc = (JCheckBox) checkBoxes.get(j + (16 * i));
                if(jc.isSelected())
                {
                    tracklist[j] = key;
                }
                else
                    tracklist[j] = 0;
            }
            makeTracks(tracklist);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }
        track.add(makeEvent(192, 9, 1, 0, 15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);;
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }
    public void makeTracks(int[] trackList)
    {
        for(int i = 0; i < 16; i++)
        {
            int key = trackList[i];
            if(key != 0)
            {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i+1));
            }
        }
    }
    public MidiEvent makeEvent(int command, int channel, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage message = new ShortMessage();
            message.setMessage(command, channel, one, two);
            event = new MidiEvent(message, tick);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
        return event;

    }
    public void setUpMidi(){
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
}
    public class MenuListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            fileChooser = new JFileChooser();
            fileChooser.showOpenDialog(frame);

        }
    }
    public class MySendListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxState = new boolean[256];
            for(int i = 0; i < 256; i++)
            {
                JCheckBox check = (JCheckBox)checkBoxes.get(i);
                if(check.isSelected())
                {
                    checkboxState[i] = true;
                }
            }
            try {

                fileChooser = new JFileChooser();
                fileChooser.showOpenDialog(frame);
                FileOutputStream fileOutputStream = new FileOutputStream(fileChooser.getSelectedFile());
                ObjectOutputStream os = new ObjectOutputStream(fileOutputStream);
                os.writeObject(checkboxState);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println("Saved");
        }

    }
    public class MyReadInListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxState = null;
            try {

                fileChooser = new JFileChooser();
                fileChooser.showOpenDialog(frame);
                FileInputStream inputStream = new FileInputStream(fileChooser.getSelectedFile());
                ObjectInputStream is = new ObjectInputStream(inputStream);
                checkboxState = (boolean[])is.readObject();
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            for(int i = 0; i < 256; i++)
            {
                JCheckBox check = (JCheckBox)checkBoxes.get(i);
                if(checkboxState[i])
                {
                    check.setSelected(true);
                }
                else
                {
                 check.setSelected(false);
                }
            }
            System.out.println("Loaded");
            sequencer.stop();
            addTrackAndStart();
        }
    }
    public class MyStartListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            addTrackAndStart();
        }
    }
    public class MyStopListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }
    public class MyUpTempoListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
           float tempoFactor = sequencer.getTempoFactor();
           sequencer.setTempoFactor((float)(tempoFactor * 1.03));
        }
    }
    public class MyDownTempoListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {

            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 0.97));
        }
    }
}