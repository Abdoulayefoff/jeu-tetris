package views.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicSliderUI;

import model.Evaluateur;
import model.Game;
import model.GameConfig;
import model.GameSerializer;
import model.MultiplayerGame;
import model.ai.AI;
import model.ai.AdvancedAI;
import model.ai.GreedyAI;
import model.ai.RandomAI;
import observer.ListeningModel;
import piece.Coordinates;
import piece.Piece;

/**
 * Main panel of the window: the cutting mat on the left, one column of readouts and
 * controls on the right, and a status bar that says what to do next.
 */
public class Rendu extends JPanel implements ListeningModel {

    private static final long serialVersionUID = 1L;
    private static final int COLUMN_WIDTH = 318;
    private static final int GUTTER = 24;

    protected final Game game;
    private final MultiplayerGame multiGame; // null if the game is single-player only
    protected final BoardView board;

    // controls
    protected JButton startBtn;
    protected JButton endBtn;
    protected JButton saveBtn;
    protected JButton loadBtn;
    private SegmentedControl layoutSelect;
    private SegmentedControl modeSelect;
    protected JButton rotateCWBtn;
    protected JButton rotateACWBtn;
    protected JButton playAIBtn;
    protected JButton advancedAIBtn;
    protected JButton greedyAIBtn;
    protected JButton stopAIBtn;
    private JSlider speedSlider;

    // readouts
    protected JLabel scoreValueLabel;
    private JLabel scoreNoteLabel;
    private JLabel actionsValueLabel;
    protected ActionRuler actionsRuler;
    protected JLabel areaValueLabel;
    private JLabel areaNoteLabel;
    protected JLabel piecesValueLabel;
    private JPanel playersPanel;
    private JPanel aiGroup;
    private JLabel player1Label;
    private JLabel player1Score;
    private JLabel player2Label;
    private JLabel player2Score;
    private JLabel hintLabel;

    private boolean hasStarted = false;
    private boolean gameOverShown = false;
    private final JFileChooser fileChooser = new JFileChooser();
    private final String menuKeyName;

    public Rendu(Game game, BoardView board) {
        super(new BorderLayout());
        this.game = game;
        this.multiGame = (game instanceof MultiplayerGame) ? (MultiplayerGame) game : null;
        this.board = board;
        int menu = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        this.menuKeyName = (menu == InputEvent.META_DOWN_MASK) ? "Cmd" : "Ctrl";
        setBackground(Theme.PANEL);

        add(createMat(), BorderLayout.CENTER);
        add(createColumn(), BorderLayout.EAST);
        add(createStatusBar(), BorderLayout.SOUTH);

        fileChooser.setFileFilter(new FileNameExtensionFilter(Constants.SAVE_FILE_FILTER, "json"));
        manageEvents();
        installKeyBindings();

        game.addListening(this);
        board.addSelectionListener(this::updateGame);
        updateGame();
    }

    // ================================================================== construction

    /** The mat keeps square cells and sits centred on the workbench, whatever the window size. */
    private JComponent createMat() {
        JPanel holder = new JPanel(null) {
            private static final long serialVersionUID = 1L;

            @Override
            public void doLayout() {
                int cols = board.getPlateau().getWidth();
                int rows = board.getPlateau().getHeight();
                int cell = Math.max(4, Math.min((getWidth() - 2 * GUTTER) / cols, (getHeight() - 2 * GUTTER) / rows));
                int w = cols * cell + 1;
                int h = rows * cell + 1;
                board.setBounds((getWidth() - w) / 2, (getHeight() - h) / 2, w, h);
                board.doLayout();
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = board.getPreferredSize();
                return new Dimension(d.width + 2 * GUTTER, d.height + 2 * GUTTER);
            }
        };
        holder.setBackground(Theme.PANEL);
        holder.add(board);
        return holder;
    }

    private JComponent createColumn() {
        JPanel col = new ColumnPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(Theme.PANEL);
        col.setBorder(BorderFactory.createEmptyBorder(20, 4, 16, GUTTER));

        JLabel title = text(Constants.WINDOW_TITLE, Theme.demi(17f), Theme.INK);
        col.add(title);
        col.add(Box.createVerticalStrut(14));

        // score
        col.add(text(Constants.SCORE_LABEL, Theme.medium(12f), Theme.INK_SOFT));
        scoreValueLabel = text("—", Theme.demi(42f), Theme.INK);
        col.add(scoreValueLabel);
        scoreNoteLabel = text(" ", Theme.regular(12f), Theme.INK_SOFT);
        col.add(scoreNoteLabel);
        col.add(Box.createVerticalStrut(14));

        // actions
        actionsValueLabel = text("", Theme.demi(13f), Theme.INK);
        col.add(row(text(Constants.ACTIONS_LABEL, Theme.medium(12f), Theme.INK_SOFT), actionsValueLabel));
        col.add(Box.createVerticalStrut(4));
        actionsRuler = new ActionRuler(GameConfig.MAX_ACTIONS);
        actionsRuler.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(actionsRuler);
        col.add(Box.createVerticalStrut(8));

        // bounding rectangle
        areaValueLabel = text("—", Theme.demi(13f), Theme.INK);
        col.add(row(text(Constants.RECTANGLE_LABEL, Theme.medium(12f), Theme.INK_SOFT), areaValueLabel));
        areaNoteLabel = text(" ", Theme.regular(12f), Theme.INK_SOFT);
        col.add(areaNoteLabel);
        piecesValueLabel = text(" ", Theme.regular(12f), Theme.INK_SOFT);
        col.add(piecesValueLabel);

        // players (two-player mode only)
        playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setOpaque(false);
        playersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        playersPanel.add(Box.createVerticalStrut(14));
        playersPanel.add(text(Constants.PLAYERS_LABEL, Theme.medium(12f), Theme.INK_SOFT));
        playersPanel.add(Box.createVerticalStrut(4));
        player1Label = text("", Theme.regular(13f), Theme.INK);
        player1Label.setIcon(swatch(Theme.PLAYER_1));
        player1Score = text("", Theme.demi(13f), Theme.INK);
        playersPanel.add(row(player1Label, player1Score));
        player2Label = text("", Theme.regular(13f), Theme.INK);
        player2Label.setIcon(swatch(Theme.PLAYER_2));
        player2Score = text("", Theme.demi(13f), Theme.INK);
        playersPanel.add(Box.createVerticalStrut(2));
        playersPanel.add(row(player2Label, player2Score));
        col.add(playersPanel);

        // game
        col.add(rule());
        startBtn = new FlatButton(Constants.START_GAME_LABEL, true);
        endBtn = new FlatButton(Constants.END_GAME_LABEL, false);
        col.add(heading(Constants.GAME_GROUP));
        col.add(pair(startBtn, endBtn));
        col.add(Box.createVerticalStrut(10));
        layoutSelect = new SegmentedControl(Constants.DEFAULT_CONFIG_LABEL, Constants.RANDOM_CONFIG_LABEL);
        layoutSelect.setSelectedIndex(game.getBoard().randomPlacementStrategy() ? 1 : 0);
        col.add(labelled(Constants.LAYOUT_LABEL, layoutSelect));
        modeSelect = new SegmentedControl(Constants.ONE_PLAYER_LABEL, Constants.TWO_PLAYERS_LABEL);
        if (multiGame != null) {
            col.add(Box.createVerticalStrut(8));
            col.add(labelled(Constants.MODE_LABEL, modeSelect));
        }

        // piece
        col.add(rule());
        rotateACWBtn = new FlatButton(Constants.ROTATE_ACW_LABEL, false);
        rotateCWBtn = new FlatButton(Constants.ROTATE_CW_LABEL, false);
        col.add(heading(Constants.PIECE_GROUP));
        col.add(pair(rotateACWBtn, rotateCWBtn));

        // computer (solo games only: the whole group is hidden in two-player mode)
        aiGroup = new JPanel();
        aiGroup.setLayout(new BoxLayout(aiGroup, BoxLayout.Y_AXIS));
        aiGroup.setOpaque(false);
        aiGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(aiGroup);
        aiGroup.add(rule());
        playAIBtn = new FlatButton(Constants.RANDOM_AI_LABEL, false);
        advancedAIBtn = new FlatButton(Constants.ADVANCED_AI_LABEL, false);
        greedyAIBtn = new FlatButton(Constants.GREEDY_AI_LABEL, false);
        aiGroup.add(heading(Constants.AI_GROUP));
        JPanel ais = new JPanel(new GridLayout(1, 3, 6, 0));
        ais.setOpaque(false);
        ais.add(playAIBtn);
        ais.add(advancedAIBtn);
        ais.add(greedyAIBtn);
        aiGroup.add(fill(ais));
        aiGroup.add(Box.createVerticalStrut(10));
        speedSlider = new JSlider(GameConfig.AI_MIN_DELAY_MS, GameConfig.AI_MAX_DELAY_MS, game.getAIDelay());
        speedSlider.setInverted(true); // right = faster
        speedSlider.setFocusable(false);
        speedSlider.setOpaque(false);
        speedSlider.setUI(new RulerSliderUI(speedSlider));
        JPanel speed = new JPanel(new BorderLayout(8, 0));
        speed.setOpaque(false);
        speed.add(text(Constants.SLOW_LABEL, Theme.regular(12f), Theme.INK_SOFT), BorderLayout.WEST);
        speed.add(speedSlider, BorderLayout.CENTER);
        speed.add(text(Constants.FAST_LABEL, Theme.regular(12f), Theme.INK_SOFT), BorderLayout.EAST);
        aiGroup.add(labelled(Constants.AI_SPEED_LABEL, speed));
        aiGroup.add(Box.createVerticalStrut(8));
        stopAIBtn = new FlatButton(Constants.STOP_AI_LABEL, false);
        aiGroup.add(fill(stopAIBtn));

        // file
        col.add(rule());
        saveBtn = new FlatButton(Constants.SAVE_LABEL, false);
        loadBtn = new FlatButton(Constants.LOAD_LABEL, false);
        col.add(heading(Constants.FILE_GROUP));
        col.add(pair(saveBtn, loadBtn));
        col.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(col, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.PANEL);
        scroll.setPreferredSize(new Dimension(COLUMN_WIDTH, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JComponent createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(Theme.PANEL_DEEP);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.HAIRLINE),
                BorderFactory.createEmptyBorder(9, GUTTER, 10, GUTTER)));
        hintLabel = text(" ", Theme.regular(13f), Theme.INK);
        bar.add(hintLabel, BorderLayout.CENTER);
        bar.add(text(String.format(Constants.HINT_FILE, menuKeyName, menuKeyName), Theme.regular(12f), Theme.INK_SOFT),
                BorderLayout.EAST);
        return bar;
    }

    // ------------------------------------------------------------------ small builders

    private static JLabel text(String s, java.awt.Font font, Color color) {
        JLabel l = new JLabel(s);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JComponent heading(String s) {
        JLabel l = text(s, Theme.demi(13f), Theme.INK);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return l;
    }

    /** A horizontal hairline separating two groups of controls. */
    private static JComponent rule() {
        JComponent r = new JComponent() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Theme.HAIRLINE);
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        r.setPreferredSize(new Dimension(10, 24));
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return r;
    }

    /** A label on the left, a value on the right. */
    private static JComponent row(JComponent left, JComponent right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return fill(p);
    }

    /** A small label column followed by a control. */
    private static JComponent labelled(String label, JComponent control) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel l = text(label, Theme.regular(12f), Theme.INK_SOFT);
        l.setPreferredSize(new Dimension(76, 20));
        p.add(l, BorderLayout.WEST);
        p.add(control, BorderLayout.CENTER);
        return fill(p);
    }

    private static JComponent pair(JComponent a, JComponent b) {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));
        p.setOpaque(false);
        p.add(a);
        p.add(b);
        return fill(p);
    }

    private static JComponent fill(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    /** A small square of colour, used as the player marker. */
    private static Icon swatch(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRect(x, y + 1, 10, 10);
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
        };
    }

    /** The control column: always as wide as its viewport, scrolls only vertically. */
    private static final class ColumnPanel extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return r.height - 32;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Thin ruler-like slider: a hairline track and a narrow ink cursor. */
    private static final class RulerSliderUI extends BasicSliderUI {
        RulerSliderUI(JSlider slider) {
            super(slider);
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(8, 18);
        }

        @Override
        public void paintTrack(Graphics g) {
            Rectangle t = trackRect;
            int cy = t.y + t.height / 2;
            g.setColor(Theme.HAIRLINE);
            g.fillRect(t.x, cy - 1, t.width, 2);
            g.setColor(Theme.INK_FAINT);
            for (int i = 0; i <= 4; i++) {
                int x = t.x + i * (t.width - 1) / 4;
                g.fillRect(x, cy - 4, 1, 8);
            }
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle r = thumbRect;
            g2.setColor(slider.isEnabled() ? Theme.INK : Theme.INK_FAINT);
            g2.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 4, 4);
            g2.dispose();
        }

        @Override
        public void paintFocus(Graphics g) {
            // no focus ring: the slider is not focusable
        }
    }

    // ================================================================== events

    public void manageEvents() {
        startBtn.addActionListener(e -> {
            board.setSelectedPiece(null);
            game.start();
            startBtn.setText(Constants.RESTART_GAME_LABEL);
        });

        layoutSelect.onChange(i -> {
            if (!game.useRandomPlacementStrategy(i == 1)) {
                warn(Constants.WARNING_CHANGE_CONFIG_LABEL);
                layoutSelect.setSelectedIndex(game.getBoard().randomPlacementStrategy() ? 1 : 0);
            }
        });

        modeSelect.onChange(i -> {
            if (multiGame != null && !multiGame.setTwoPlayers(i == 1)) {
                warn(Constants.WARNING_CHANGE_MODE_LABEL);
            }
            updateGame();
        });

        endBtn.addActionListener(e -> {
            game.stopAI();
            game.gameOver();
        });

        playAIBtn.addActionListener(e -> startAI(new RandomAI()));
        advancedAIBtn.addActionListener(e -> startAI(new AdvancedAI()));
        greedyAIBtn.addActionListener(e -> startAI(new GreedyAI()));
        stopAIBtn.addActionListener(e -> game.stopAI());
        speedSlider.addChangeListener(e -> game.setAIDelay(speedSlider.getValue()));

        rotateCWBtn.addActionListener(e -> rotateSelected(true, true));
        rotateACWBtn.addActionListener(e -> rotateSelected(false, true));

        saveBtn.addActionListener(e -> save());
        loadBtn.addActionListener(e -> load());
    }

    private void installKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        int menu = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        bind(im, KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "rotateCW", () -> rotateSelected(true, false));
        bind(im, KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "rotateACW", () -> rotateSelected(false, false));
        bind(im, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up", () -> nudgeSelected(-1, 0));
        bind(im, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down", () -> nudgeSelected(1, 0));
        bind(im, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left", () -> nudgeSelected(0, -1));
        bind(im, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right", () -> nudgeSelected(0, 1));
        bind(im, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "deselect", () -> board.setSelectedPiece(null));
        bind(im, KeyStroke.getKeyStroke(KeyEvent.VK_S, menu), "save", this::save);
        bind(im, KeyStroke.getKeyStroke(KeyEvent.VK_O, menu), "load", this::load);
        if (menu != InputEvent.CTRL_DOWN_MASK) { // on macOS also accept Ctrl
            bind(im, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save", this::save);
            bind(im, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "load", this::load);
        }
    }

    private void bind(InputMap im, KeyStroke key, String name, Runnable action) {
        im.put(key, name);
        getActionMap().put(name, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    // ================================================================== actions

    private boolean humanCanPlay(boolean explain) {
        if (!game.canMove()) {
            if (explain) {
                warn(Constants.WARNING_START_GAME_BEFORE_LABEL);
            }
            return false;
        }
        return !game.isAIPlaying();
    }

    private void rotateSelected(boolean clockwise, boolean explain) {
        if (!humanCanPlay(explain)) {
            return;
        }
        Piece piece = board.getSelectedPiece();
        if (piece == null) {
            if (explain) {
                warn(Constants.SELECT_PIECE_LABEL);
            }
            return;
        }
        if (!game.rotatePiece(piece, clockwise)) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void nudgeSelected(int dRow, int dCol) {
        Piece piece = board.getSelectedPiece();
        if (piece == null || !humanCanPlay(false)) {
            return;
        }
        Coordinates c = piece.getOutterCenter();
        if (!game.movePiece(piece, new Coordinates(c.getX() + dRow, c.getY() + dCol))) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void startAI(AI ai) {
        if (!game.canMove()) {
            warn(Constants.WARNING_START_GAME_BEFORE_LABEL);
            return;
        }
        if (multiGame != null && multiGame.isTwoPlayers()) {
            warn(Constants.WARNING_AI_TWO_PLAYERS_LABEL);
            return;
        }
        board.setSelectedPiece(null);
        game.playAI(ai);
        updateGame();
    }

    private void save() {
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fileChooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".json")) {
            file = new File(file.getParentFile(), file.getName() + ".json");
        }
        try {
            GameSerializer.save(game, file.toPath());
        } catch (IOException ex) {
            error(Constants.SAVE_ERROR_LABEL + ex.getMessage());
        }
    }

    private void load() {
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            board.setSelectedPiece(null);
            GameSerializer.load(fileChooser.getSelectedFile().toPath(), game);
            // a loaded finished game must not pop the "game over" dialog again
            hasStarted = game.isOnGoing();
            gameOverShown = !game.isOnGoing();
            startBtn.setText(Constants.RESTART_GAME_LABEL);
            updateGame();
        } catch (IOException | IllegalArgumentException ex) {
            error(Constants.LOAD_ERROR_LABEL + ex.getMessage());
        }
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, Constants.WARNING_LABEL, JOptionPane.WARNING_MESSAGE);
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, Constants.ERROR_LABEL, JOptionPane.ERROR_MESSAGE);
    }

    // ================================================================== refresh

    /** Refresh every widget from the model. */
    public void updateGame() {
        boolean onGoing = game.isOnGoing();
        boolean aiPlaying = game.isAIPlaying();
        boolean canMove = game.canMove();
        boolean twoPlayers = multiGame != null && multiGame.isTwoPlayers();
        int boardArea = game.getBoard().getWidth() * game.getBoard().getHeight();
        int area = game.getBoard().evaluate();
        int pieces = game.getBoard().getPieces().size();

        if (onGoing || hasStarted) {
            startBtn.setText(Constants.RESTART_GAME_LABEL);
        }

        // score: live during the game, final afterwards
        scoreValueLabel.setForeground(Theme.INK);
        if (onGoing) {
            scoreValueLabel.setText(String.valueOf(game.getCurrentScore()));
            scoreNoteLabel.setText(String.format(Constants.SCORE_LIVE_NOTE, boardArea));
        } else if (hasStarted) {
            scoreValueLabel.setText(String.valueOf(game.getScore()));
            scoreNoteLabel.setText(String.format(Constants.SCORE_FINAL_NOTE, boardArea));
        } else {
            scoreValueLabel.setText("—");
            scoreValueLabel.setForeground(Theme.INK_FAINT);
            scoreNoteLabel.setText(Constants.SCORE_IDLE_NOTE);
        }

        int remaining = game.getRemainingActions();
        actionsValueLabel.setText(remaining + " / " + GameConfig.MAX_ACTIONS);
        actionsValueLabel.setForeground(onGoing && remaining <= 5 ? Theme.ALERT : Theme.INK);
        actionsRuler.setRemaining(remaining, onGoing);

        int[] box = Evaluateur.boundingBox(game.getBoard().getPieces());
        if (box == null) {
            areaValueLabel.setText("—");
            areaNoteLabel.setText(" ");
            piecesValueLabel.setText(" ");
        } else {
            areaValueLabel.setText((box[3] - box[1] + 1) + " × " + (box[2] - box[0] + 1));
            areaNoteLabel.setText(String.format(Constants.AREA_NOTE, area, Math.round(100f * area / boardArea)));
            piecesValueLabel.setText(String.format(Constants.PIECES_NOTE, pieces));
        }

        playersPanel.setVisible(twoPlayers);
        if (twoPlayers) {
            int current = multiGame.getCurrentPlayer();
            setPlayerRow(player1Label, player1Score, 1, onGoing && current == 1);
            setPlayerRow(player2Label, player2Score, 2, onGoing && current == 2);
        }
        if (multiGame != null) {
            modeSelect.setSelectedIndex(twoPlayers ? 1 : 0);
            modeSelect.setEnabled(!onGoing);
        }
        layoutSelect.setSelectedIndex(game.getBoard().randomPlacementStrategy() ? 1 : 0);
        layoutSelect.setEnabled(!onGoing);

        boolean humanTurn = canMove && !aiPlaying;
        boolean hasSelection = board.getSelectedPiece() != null;
        rotateCWBtn.setEnabled(humanTurn && hasSelection);
        rotateACWBtn.setEnabled(humanTurn && hasSelection);
        playAIBtn.setEnabled(humanTurn && !twoPlayers);
        advancedAIBtn.setEnabled(humanTurn && !twoPlayers);
        greedyAIBtn.setEnabled(humanTurn && !twoPlayers);
        aiGroup.setVisible(!twoPlayers);
        stopAIBtn.setEnabled(aiPlaying);
        endBtn.setEnabled(onGoing);
        saveBtn.setEnabled(!aiPlaying);
        loadBtn.setEnabled(!aiPlaying);

        hintLabel.setText(hint(onGoing, aiPlaying, hasSelection, twoPlayers));

        // game over dialog: exactly once per game
        if (onGoing) {
            hasStarted = true;
            gameOverShown = false;
        } else if (hasStarted && !gameOverShown && !aiPlaying) {
            gameOverShown = true;
            SwingUtilities.invokeLater(this::showGameOver);
        }
    }

    private void setPlayerRow(JLabel name, JLabel score, int player, boolean current) {
        name.setText(Constants.PLAYER_LABEL + player + (current ? Constants.YOUR_TURN_SUFFIX : ""));
        name.setFont(current ? Theme.demi(13f) : Theme.regular(13f));
        score.setText(String.valueOf(multiGame.getPlayerScore(player)));
    }

    /** The one sentence telling the player what they can do right now. */
    private String hint(boolean onGoing, boolean aiPlaying, boolean hasSelection, boolean twoPlayers) {
        if (aiPlaying) {
            return Constants.HINT_AI;
        }
        if (!onGoing) {
            return hasStarted ? Constants.HINT_OVER : Constants.HINT_IDLE;
        }
        String hint = hasSelection ? Constants.HINT_SELECTED : Constants.HINT_PLAY;
        if (!twoPlayers) {
            return hint;
        }
        // "Joueur 2 : cliquez sur…": the sentence continues after the colon
        return String.format(Constants.HINT_TURN, multiGame.getCurrentPlayer())
                + Character.toLowerCase(hint.charAt(0)) + hint.substring(1);
    }

    private void showGameOver() {
        String detail;
        if (multiGame != null && multiGame.isTwoPlayers()) {
            int s1 = multiGame.getPlayerScore(1);
            int s2 = multiGame.getPlayerScore(2);
            int winner = multiGame.getWinner();
            detail = (winner == 0)
                    ? String.format(Constants.DRAW_LABEL, s1)
                    : String.format(Constants.WINNER_LABEL, winner, Math.max(s1, s2), Math.min(s1, s2));
        } else {
            int[] box = Evaluateur.boundingBox(game.getBoard().getPieces());
            detail = box == null ? " " : String.format(Constants.GAME_OVER_LABEL,
                    box[3] - box[1] + 1, box[2] - box[0] + 1, game.getBoard().evaluate());
        }
        showGameOverDialog(game.getScore(), detail);
    }

    private void showGameOverDialog(int score, String detail) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Constants.GAME_OVER_TITLE, JDialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(26, 30, 24, 30));
        p.add(text(Constants.GAME_OVER_HEADING, Theme.demi(15f), Theme.INK_SOFT));
        p.add(text(String.valueOf(score), Theme.demi(56f), Theme.INK));
        p.add(text(detail, Theme.regular(13f), Theme.INK));
        p.add(Box.createVerticalStrut(22));
        JButton again = new FlatButton(Constants.PLAY_AGAIN_LABEL, true);
        JButton close = new FlatButton(Constants.CLOSE_LABEL, false);
        again.addActionListener(e -> {
            dialog.dispose();
            startBtn.doClick();
        });
        close.addActionListener(e -> dialog.dispose());
        p.add(pair(again, close));
        dialog.setContentPane(p);
        dialog.getRootPane().setDefaultButton(again);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(340, dialog.getHeight()));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    @Override
    public void modelUpdated(Object source) {
        if (SwingUtilities.isEventDispatchThread()) {
            updateGame();
        } else {
            SwingUtilities.invokeLater(this::updateGame);
        }
    }

    public Game getGame() {
        return game;
    }

    public BoardView getBoard() {
        return board;
    }
}
