import java.awt.EventQueue;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class MessageFerry extends JFrame{

	public MessageFerry() {
		Board b = new Board();
		b.setFocusable(true);
        add(b);
        setTitle("Message Ferry Simulator");
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public static void main(String[] args) {
    	EventQueue.invokeLater(new Runnable()
    	{
    		@Override
    		public void run()
    		{
    			JFrame ex = new MessageFerry();
    			ex.setVisible(true);
    		}
    	});
    }
}
