import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.*;
import javax.swing.Timer;

public class NexusHub extends JFrame {

    // ── Color Palette ──────────────────────────────────────────────────────────
    static final Color BG_DARK      = new Color(10, 12, 20);
    static final Color BG_PANEL     = new Color(16, 20, 35);
    static final Color BG_CARD      = new Color(22, 28, 48);
    static final Color BG_CARD2     = new Color(28, 35, 60);
    static final Color ACCENT_CYAN  = new Color(0, 212, 255);
    static final Color ACCENT_PINK  = new Color(255, 60, 150);
    static final Color ACCENT_GOLD  = new Color(255, 196, 0);
    static final Color ACCENT_GREEN = new Color(0, 255, 140);
    static final Color TEXT_PRIMARY = new Color(230, 240, 255);
    static final Color TEXT_MUTED   = new Color(120, 140, 180);
    static final Color TABLE_HDR    = new Color(18, 24, 42);

    // ── DB Config ──────────────────────────────────────────────────────────────
    private static final String DB_URL = "jdbc:sqlserver://DESKTOP-PDMFNTL:1433;databaseName=FreelancerManagementDB;encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "pakistan";
    // ── State ──────────────────────────────────────────────────────────────────
    private Connection conn;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel statusLabel;
    private JLabel connLabel;
    private String activeSection = "dashboard";
    private Map<String, JButton> navButtons = new LinkedHashMap<>();

    // Animated particles
    private float[] px, py, pvx, pvy;
    private float[] palpha;
    private static final int PCOUNT = 55;
    private Timer particleTimer;
    private BackgroundPanel bgPanel;

    public NexusHub() {
        initParticles();
        setupFrame();
        buildUI();
        animateIn();
        startParticles();
        tryConnect();
    }

    // ── Particle System ────────────────────────────────────────────────────────
    private void initParticles() {
        px   = new float[PCOUNT]; py   = new float[PCOUNT];
        pvx  = new float[PCOUNT]; pvy  = new float[PCOUNT];
        palpha = new float[PCOUNT];
        Random r = new Random();
        for (int i = 0; i < PCOUNT; i++) {
            px[i]  = r.nextFloat() * 1600;
            py[i]  = r.nextFloat() * 900;
            pvx[i] = (r.nextFloat() - 0.5f) * 0.4f;
            pvy[i] = (r.nextFloat() - 0.5f) * 0.4f;
            palpha[i] = 0.1f + r.nextFloat() * 0.35f;
        }
    }

    private void startParticles() {
        particleTimer = new Timer(30, e -> {
            for (int i = 0; i < PCOUNT; i++) {
                px[i] += pvx[i]; py[i] += pvy[i];
                if (px[i] < 0) px[i] = getWidth();
                if (px[i] > getWidth()) px[i] = 0;
                if (py[i] < 0) py[i] = getHeight();
                if (py[i] > getHeight()) py[i] = 0;
            }
            if (bgPanel != null) bgPanel.repaint();
        });
        particleTimer.start();
    }

    // ── Frame Setup ────────────────────────────────────────────────────────────
    private void setupFrame() {
        setTitle("NexusHub — Freelancer Command Center");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setUndecorated(false);
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
    }

    // ── Root UI ────────────────────────────────────────────────────────────────
    private void buildUI() {
        bgPanel = new BackgroundPanel();
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);

        bgPanel.add(buildSidebar(), BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(buildTopBar(), BorderLayout.NORTH);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        contentPanel.add(buildDashboard(), "dashboard");
        contentPanel.add(buildTableView("Freelancers",
            "SELECT freelancer_id,name,phone,skill,experience_years FROM Freelancers"), "freelancers");
        contentPanel.add(buildTableView("Clients",
            "SELECT client_id,name,email,company_address FROM Clients"), "clients");
        contentPanel.add(buildTableView("Projects",
            "SELECT project_id,title,description,budget,freelancer_id,client_id FROM Projects"), "projects");
        contentPanel.add(buildTableView("Payments",
            "SELECT payment_id,payment_date,status,amount,freelancer_id,client_id,project_id FROM Payments"), "payments");
        contentPanel.add(buildQueryPanel(), "query");

        center.add(contentPanel, BorderLayout.CENTER);
        center.add(buildStatusBar(), BorderLayout.SOUTH);
        bgPanel.add(center, BorderLayout.CENTER);
    }

    // ── Background Panel ───────────────────────────────────────────────────────
    class BackgroundPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Deep gradient background
            GradientPaint gp = new GradientPaint(0,0, BG_DARK, getWidth(), getHeight(), new Color(8,10,28));
            g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());

            // Grid lines
            g2.setColor(new Color(40, 60, 120, 20));
            g2.setStroke(new BasicStroke(0.5f));
            for (int x = 0; x < getWidth(); x += 60)  g2.drawLine(x,0,x,getHeight());
            for (int y = 0; y < getHeight(); y += 60)  g2.drawLine(0,y,getWidth(),y);

            // Glow orbs
            drawOrb(g2, getWidth()*0.15f, getHeight()*0.2f,  180, ACCENT_CYAN, 0.06f);
            drawOrb(g2, getWidth()*0.85f, getHeight()*0.75f, 160, ACCENT_PINK, 0.05f);
            drawOrb(g2, getWidth()*0.5f,  getHeight()*0.9f,  120, ACCENT_GOLD, 0.04f);

            // Particles
            for (int i = 0; i < PCOUNT; i++) {
                Color pc = (i % 3 == 0) ? ACCENT_CYAN : (i % 3 == 1) ? ACCENT_PINK : ACCENT_GOLD;
                g2.setColor(new Color(pc.getRed(), pc.getGreen(), pc.getBlue(), (int)(palpha[i]*255)));
                float sz = 1.5f + (i % 5) * 0.6f;
                g2.fill(new Ellipse2D.Float(px[i]-sz/2, py[i]-sz/2, sz, sz));
            }
            super.paintChildren(g);
        }

        private void drawOrb(Graphics2D g2, float x, float y, float r, Color c, float alpha) {
            RadialGradientPaint rg = new RadialGradientPaint(
                new Point2D.Float(x, y), r,
                new float[]{0f, 1f},
                new Color[]{new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha*255)),
                            new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)}
            );
            g2.setPaint(rg);
            g2.fill(new Ellipse2D.Float(x-r, y-r, r*2, r*2));
        }
    }

    // ── Sidebar ────────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel side = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, new Color(12,16,32,230),
                    getWidth(), getHeight(), new Color(18,24,48,230));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                // Right border glow
                g2.setColor(new Color(0,212,255,60));
                g2.fillRect(getWidth()-2, 0, 2, getHeight());
            }
        };
        side.setPreferredSize(new Dimension(230, 0));
        side.setLayout(new BorderLayout());
        side.setOpaque(false);

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(30, 10, 20, 10));

        JLabel logo = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                // "Nexus" in cyan
                g2.setColor(ACCENT_CYAN);
                g2.drawString("Nexus", 0, fm.getAscent());
                // "Hub" in gold
                g2.setColor(ACCENT_GOLD);
                g2.drawString("Hub", g2.getFontMetrics().stringWidth("Nexus")+4, fm.getAscent());
            }
            @Override public Dimension getPreferredSize() { return new Dimension(130, 40); }
        };
        logoPanel.add(logo);

        JLabel tagline = new JLabel("Command Center", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        tagline.setForeground(TEXT_MUTED);
        JPanel logoWrap = new JPanel(new BorderLayout());
        logoWrap.setOpaque(false);
        logoWrap.add(logoPanel, BorderLayout.CENTER);
        logoWrap.add(tagline, BorderLayout.SOUTH);
        side.add(logoWrap, BorderLayout.NORTH);

        // Nav
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(10, 12, 10, 12));

        String[][] sections = {
            {"dashboard","⬡","Dashboard"},
            {"freelancers","◈","Freelancers"},
            {"clients","◎","Clients"},
            {"projects","⬟","Projects"},
            {"payments","◆","Payments"},
            {"query","⌗","SQL Query"}
        };

        Color[] sectionColors = {ACCENT_CYAN, ACCENT_GREEN, ACCENT_PINK, ACCENT_GOLD,
                                  new Color(160,120,255), new Color(255,140,60)};

        for (int i = 0; i < sections.length; i++) {
            final String sec  = sections[i][0];
            final Color  col  = sectionColors[i];
            NavButton btn = new NavButton(sections[i][1], sections[i][2], col);
            btn.addActionListener(e -> navigate(sec, col));
            nav.add(btn);
            nav.add(Box.createVerticalStrut(6));
            navButtons.put(sec, btn);
        }

        side.add(nav, BorderLayout.CENTER);

        // Bottom connection badge
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 14, 20, 14));
        connLabel = new JLabel("● Not Connected", SwingConstants.CENTER);
        connLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        connLabel.setForeground(ACCENT_PINK);
        connLabel.setBorder(new EmptyBorder(8, 0, 8, 0));
        bottom.add(connLabel, BorderLayout.CENTER);

        JButton reconnBtn = createActionButton("Reconnect DB", ACCENT_CYAN);
        reconnBtn.addActionListener(e -> tryConnect());
        bottom.add(reconnBtn, BorderLayout.SOUTH);
        side.add(bottom, BorderLayout.SOUTH);

        return side;
    }

    // ── Nav Button ─────────────────────────────────────────────────────────────
    class NavButton extends JButton {
        private Color accent;
        private float glowAmt = 0f;
        private Timer glowTimer;
        private boolean active = false;

        NavButton(String icon, String label, Color accent) {
            this.accent = accent;
            setText("  " + icon + "  " + label);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(TEXT_MUTED);
            setOpaque(false); setContentAreaFilled(false);
            setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(220, 42));
            setPreferredSize(new Dimension(200, 42));
            setHorizontalAlignment(SwingConstants.LEFT);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { animateGlow(true); }
                public void mouseExited(MouseEvent e)  { if (!active) animateGlow(false); }
            });
        }

        void setActive(boolean b) {
            active = b;
            if (b) { glowAmt = 1f; setForeground(Color.WHITE); repaint(); }
            else    { animateGlow(false); }
        }

        private void animateGlow(boolean in) {
            if (glowTimer != null) glowTimer.stop();
            glowTimer = new Timer(16, null);
            glowTimer.addActionListener(e -> {
                glowAmt += in ? 0.08f : -0.08f;
                glowAmt = Math.max(0, Math.min(1, glowAmt));
                setForeground(blend(TEXT_MUTED, Color.WHITE, glowAmt));
                repaint();
                if ((in && glowAmt >= 1) || (!in && glowAmt <= 0)) glowTimer.stop();
            });
            glowTimer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (glowAmt > 0 || active) {
                float a = active ? 1f : glowAmt;
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(a*40)));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                if (active) {
                    g2.setColor(accent);
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-2,getHeight()-2,12,12));
                    // Left accent bar
                    g2.setColor(accent);
                    g2.fillRoundRect(0, 8, 3, getHeight()-16, 3, 3);
                }
            }
            super.paintComponent(g);
        }
    }

    // ── Top Bar ────────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(12,16,32,200));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(0,212,255,40));
                g2.fillRect(0,getHeight()-1,getWidth(),1);
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setBorder(new EmptyBorder(0, 24, 0, 24));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setName("topTitle");
        bar.add(title, BorderLayout.WEST);

        // Right controls
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel dateLabel = new JLabel(new java.util.Date().toString().substring(0,10));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_MUTED);
        right.add(dateLabel);

        JButton refreshBtn = createActionButton("⟳ Refresh", ACCENT_GREEN);
        refreshBtn.addActionListener(e -> refreshCurrentView());
        right.add(refreshBtn);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Status Bar ─────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(12,16,32,200));
                g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0,30));
        bar.setBorder(new EmptyBorder(0,16,0,16));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT_MUTED);
        bar.add(statusLabel, BorderLayout.WEST);

        JLabel versionLabel = new JLabel("NexusHub v1.0  |  FreelancerManagementDB");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        versionLabel.setForeground(new Color(80,100,140));
        bar.add(versionLabel, BorderLayout.EAST);
        return bar;
    }

    // ── Dashboard ──────────────────────────────────────────────────────────────
    private JPanel buildDashboard() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setOpaque(false);

        // Stat cards row
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.setPreferredSize(new Dimension(0, 140));

        String[][] stats = {
            {"Freelancers","4","◈",ACCENT_GREEN.toString()},
            {"Clients","4","◎",ACCENT_CYAN.toString()},
            {"Projects","4","⬟",ACCENT_GOLD.toString()},
            {"Payments","4","◆",ACCENT_PINK.toString()}
        };
        Color[] statColors = {ACCENT_GREEN, ACCENT_CYAN, ACCENT_GOLD, ACCENT_PINK};
        String[] queries = {
            "SELECT COUNT(*) FROM Freelancers",
            "SELECT COUNT(*) FROM Clients",
            "SELECT COUNT(*) FROM Projects",
            "SELECT COUNT(*) FROM Payments"
        };

        for (int i = 0; i < stats.length; i++) {
           
            cards.add(new StatCard(stats[i][0], stats[i][2], statColors[i], queries[i]));
        }
        p.add(cards, BorderLayout.NORTH);

        // Bottom half — recent projects + payments
        JPanel bottom = new JPanel(new GridLayout(1, 2, 16, 0));
        bottom.setOpaque(false);

        bottom.add(buildMiniTable("Recent Projects",
            "SELECT TOP 5 title,budget FROM Projects ORDER BY project_id DESC",
            new String[]{"Title","Budget"}, ACCENT_GOLD));

        bottom.add(buildMiniTable("Recent Payments",
            "SELECT TOP 5 payment_id,amount,status FROM Payments ORDER BY payment_id DESC",
            new String[]{"ID","Amount","Status"}, ACCENT_PINK));

        p.add(bottom, BorderLayout.CENTER);
        return p;
    }

    // ── Stat Card ──────────────────────────────────────────────────────────────
    class StatCard extends JPanel {
        private String label, icon, query;
        private Color accent;
        private String value = "—";
        private float hoverAmt = 0f;
        private Timer hoverTimer;

        StatCard(String label, String icon, Color accent, String query) {
            this.label = label; this.icon = icon;
            this.accent = accent; this.query = query;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { animateHover(true); }
                public void mouseExited(MouseEvent e)  { animateHover(false); }
            });
            // Async value load
            SwingUtilities.invokeLater(this::loadValue);
        }

        void loadValue() {
            if (conn == null) return;
            try {
                ResultSet rs = conn.createStatement().executeQuery(query);
                if (rs.next()) { value = rs.getString(1); repaint(); }
            } catch (Exception ignored) {}
        }

        void animateHover(boolean in) {
            if (hoverTimer != null) hoverTimer.stop();
            hoverTimer = new Timer(16, null);
            hoverTimer.addActionListener(e -> {
                hoverAmt += in ? 0.1f : -0.1f;
                hoverAmt = Math.max(0, Math.min(1, hoverAmt));
                repaint();
                if ((in && hoverAmt >= 1) || (!in && hoverAmt <= 0)) hoverTimer.stop();
            });
            hoverTimer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            float lift = hoverAmt * 4;
            int y0 = (int)lift;
            int h  = getHeight() - y0;

            // Card background
            GradientPaint gp = new GradientPaint(0,0, BG_CARD, getWidth(), getHeight(), BG_CARD2);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(4, y0, getWidth()-8, h-4, 18, 18));

            // Accent glow top bar
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
            g2.fill(new RoundRectangle2D.Float(4, y0, getWidth()-8, 3, 3, 3));

            // Hover glow
            if (hoverAmt > 0) {
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(hoverAmt*30)));
                g2.fill(new RoundRectangle2D.Float(4, y0, getWidth()-8, h-4, 18, 18));
                // Border
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(hoverAmt*120)));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(5, y0+1, getWidth()-10, h-6, 18, 18));
            }

            // Icon
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 28));
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
            g2.drawString(icon, 20, y0 + 48);

            // Value
            g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
            g2.setColor(TEXT_PRIMARY);
            g2.drawString(value, 20, y0 + 90);

            // Label
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(TEXT_MUTED);
            g2.drawString(label, 20, y0 + 112);
        }
    }

    // ── Mini Table ─────────────────────────────────────────────────────────────
    private JPanel buildMiniTable(String title, String query, String[] cols, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0,10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),18,18));
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),3,3,3));
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(accent);
        card.add(lbl, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable tbl = styledTable(model);
        tbl.setRowHeight(28);
        JScrollPane sp = new JScrollPane(tbl);
        styleScrollPane(sp);
        card.add(sp, BorderLayout.CENTER);

        // Load data async
        SwingUtilities.invokeLater(() -> {
            if (conn == null) return;
            try {
                ResultSet rs = conn.createStatement().executeQuery(query);
                while (rs.next()) {
                    Object[] row = new Object[cols.length];
                    for (int i = 0; i < cols.length; i++) row[i] = rs.getString(i+1);
                    model.addRow(row);
                }
            } catch (Exception ignored) {}
        });

        return card;
    }

    // ── Full Table View ────────────────────────────────────────────────────────
    private JPanel buildTableView(String tableName, String query) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchBar.setOpaque(false);
        JTextField searchField = new StyledTextField("  🔍  Search " + tableName + "...", 28);
        searchBar.add(searchField);

        JButton loadBtn = createActionButton("  ⟳ Load Data  ", ACCENT_CYAN);
        searchBar.add(Box.createHorizontalStrut(12));
        searchBar.add(loadBtn);
        p.add(searchBar, BorderLayout.NORTH);

        // Table
        DefaultTableModel model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);
        JScrollPane sp = new JScrollPane(table);
        styleScrollPane(sp);
        p.add(sp, BorderLayout.CENTER);

        loadBtn.addActionListener(e -> {
            setStatus("Loading " + tableName + "...");
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override protected Void doInBackground() {
                    loadTable(model, query, table);
                    return null;
                }
                @Override protected void done() { setStatus(tableName + " loaded."); }
            };
            worker.execute();
        });

        // Filter
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String txt = searchField.getText().replaceAll("🔍","").trim();
                if (table.getRowSorter() instanceof TableRowSorter<?> sorter) {
                    sorter.setRowFilter(txt.isEmpty() ? null : RowFilter.regexFilter("(?i)" + txt));
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        // Auto load on navigate
        p.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { loadBtn.doClick(); }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        return p;
    }

    private void loadTable(DefaultTableModel model, String query, JTable table) {
        if (conn == null) { setStatus("Not connected."); return; }
        try {
            ResultSet rs = conn.createStatement().executeQuery(query);
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();

            String[] colNames = new String[cols];
            for (int i = 1; i <= cols; i++) colNames[i-1] = meta.getColumnName(i);

            SwingUtilities.invokeLater(() -> {
                model.setColumnIdentifiers(colNames);
                model.setRowCount(0);
            });

            java.util.List<Object[]> rows = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int i = 0; i < cols; i++) row[i] = rs.getString(i+1);
                rows.add(row);
            }
            SwingUtilities.invokeLater(() -> {
                for (Object[] r : rows) model.addRow(r);
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
                table.setRowSorter(sorter);
            });
        } catch (SQLException ex) {
            setStatus("Error: " + ex.getMessage());
        }
    }

    // ── SQL Query Panel ────────────────────────────────────────────────────────
    private JPanel buildQueryPanel() {
        JPanel p = new JPanel(new BorderLayout(0,12));
        p.setOpaque(false);

        // Editor card
        JPanel editorCard = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),16,16));
                g2.setColor(new Color(160,120,255, 160));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),3,3,3));
            }
        };
        editorCard.setOpaque(false);
        editorCard.setBorder(new EmptyBorder(14, 14, 14, 14));
        editorCard.setPreferredSize(new Dimension(0, 200));

        JLabel edLbl = new JLabel("SQL Query Editor");
        edLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        edLbl.setForeground(new Color(160,120,255));
        editorCard.add(edLbl, BorderLayout.NORTH);

        JTextArea queryArea = new JTextArea("SELECT * FROM Freelancers;");
        queryArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        queryArea.setForeground(new Color(180, 220, 255));
        queryArea.setBackground(new Color(14, 18, 32));
        queryArea.setCaretColor(ACCENT_CYAN);
        queryArea.setBorder(new EmptyBorder(10,10,10,10));
        queryArea.setLineWrap(true);
        JScrollPane qsp = new JScrollPane(queryArea);
        styleScrollPane(qsp);
        editorCard.add(qsp, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        btnRow.setOpaque(false);
        JButton runBtn = createActionButton("  ▶  Execute Query  ", new Color(160,120,255));
        btnRow.add(runBtn);
        editorCard.add(btnRow, BorderLayout.SOUTH);
        p.add(editorCard, BorderLayout.NORTH);

        // Results
        DefaultTableModel resModel = new DefaultTableModel();
        JTable resTable = styledTable(resModel);
        JScrollPane rsp = new JScrollPane(resTable);
        styleScrollPane(rsp);
        p.add(rsp, BorderLayout.CENTER);

        runBtn.addActionListener(e -> {
            String sql = queryArea.getText().trim();
            if (sql.isEmpty() || conn == null) return;
            setStatus("Executing...");
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override protected Void doInBackground() {
                    try {
                        Statement stmt = conn.createStatement();
                        boolean hasRS = stmt.execute(sql);
                        if (hasRS) {
                            ResultSet rs = stmt.getResultSet();
                            ResultSetMetaData meta = rs.getMetaData();
                            int cols = meta.getColumnCount();
                            String[] cn = new String[cols];
                            for (int i=1;i<=cols;i++) cn[i-1]=meta.getColumnName(i);
                            java.util.List<Object[]> rows = new ArrayList<>();
                            while (rs.next()) {
                                Object[] row = new Object[cols];
                                for (int i=0;i<cols;i++) row[i]=rs.getString(i+1);
                                rows.add(row);
                            }
                            SwingUtilities.invokeLater(() -> {
                                resModel.setColumnIdentifiers(cn);
                                resModel.setRowCount(0);
                                for (Object[] r : rows) resModel.addRow(r);
                                setStatus("Query OK — " + rows.size() + " rows");
                            });
                        } else {
                            int uc = stmt.getUpdateCount();
                            SwingUtilities.invokeLater(() -> {
                                resModel.setColumnIdentifiers(new String[]{"Result"});
                                resModel.setRowCount(0);
                                resModel.addRow(new Object[]{"Success — " + uc + " row(s) affected"});
                                setStatus("Query executed.");
                            });
                        }
                    } catch (SQLException ex) {
                        SwingUtilities.invokeLater(() -> {
                            resModel.setColumnIdentifiers(new String[]{"Error"});
                            resModel.setRowCount(0);
                            resModel.addRow(new Object[]{ex.getMessage()});
                            setStatus("Error: " + ex.getMessage());
                        });
                    }
                    return null;
                }
            };
            worker.execute();
        });

        return p;
    }

    // ── Styled Components ──────────────────────────────────────────────────────
    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? BG_CARD : BG_CARD2);
                } else {
                    c.setBackground(new Color(0, 212, 255, 60));
                }
                c.setForeground(TEXT_PRIMARY);
                if (c instanceof JComponent jc) jc.setBorder(new EmptyBorder(6, 12, 6, 12));
                return c;
            }
        };
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(40, 55, 90));
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(0, 212, 255, 80));
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setDefaultRenderer(new TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel(val == null ? "" : val.toString(), SwingConstants.LEFT);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(ACCENT_CYAN);
                lbl.setBackground(TABLE_HDR);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(8, 12, 8, 12));
                return lbl;
            }
        });
        return table;
    }

    private void styleScrollPane(JScrollPane sp) {
        sp.setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(new Color(40,55,90), 1));
        sp.getViewport().setBackground(BG_CARD);
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0, 212, 255, 80); trackColor = BG_CARD;
            }
        });
    }

    private JButton createActionButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            private float glow = 0f;
            private Timer t;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { anim(true); }
                    public void mouseExited(MouseEvent e) { anim(false); }
                });
            }
            void anim(boolean in) {
                if (t != null) t.stop();
                t = new Timer(16, null);
                t.addActionListener(ev -> {
                    glow += in ? 0.1f : -0.1f;
                    glow = Math.max(0, Math.min(1, glow));
                    repaint();
                    if ((in&&glow>=1)||(!in&&glow<=0)) t.stop();
                });
                t.start();
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(60+glow*60));
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(140+glow*115)));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-2,getHeight()-2,10,10));
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(accent);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        return btn;
    }

    // ── Styled TextField ───────────────────────────────────────────────────────
    class StyledTextField extends JTextField {
        StyledTextField(String placeholder, int cols) {
            super(cols);
            setText(placeholder);
            setForeground(TEXT_MUTED);
            setBackground(BG_CARD);
            setCaretColor(ACCENT_CYAN);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 80, 130), 1),
                new EmptyBorder(6, 8, 6, 8)
            ));
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (getText().startsWith("  🔍")) { setText(""); setForeground(TEXT_PRIMARY); }
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_CYAN, 1),
                        new EmptyBorder(6, 8, 6, 8)));
                }
                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty()) { setText(placeholder); setForeground(TEXT_MUTED); }
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(60, 80, 130), 1),
                        new EmptyBorder(6, 8, 6, 8)));
                }
            });
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────────
    private void navigate(String section, Color accent) {
        // Update nav buttons
        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {
            if (e.getValue() instanceof NavButton nb) nb.setActive(e.getKey().equals(section));
        }
        cardLayout.show(contentPanel, section);
        activeSection = section;

        // Update top bar title
        
        for (Component c : ((JPanel)((BorderLayout)((JPanel)getContentPane().getComponent(1))
            .getLayout()).getLayoutComponent(BorderLayout.NORTH)).getComponents()) {
            if (c instanceof JLabel lbl && "topTitle".equals(lbl.getName())) {
                lbl.setText(section.substring(0,1).toUpperCase() + section.substring(1));
                lbl.setForeground(accent);
            }
        }
        setStatus("Navigated to " + section);
    }

    private void refreshCurrentView() {
        setStatus("Refreshing...");
        // Trigger re-load by re-showing card
        cardLayout.show(contentPanel, activeSection);
        setStatus("Refreshed");
    }

    // ── DB Connection ──────────────────────────────────────────────────────────
    private void tryConnect() {
        setStatus("Connecting to database...");
        connLabel.setText("● Connecting...");
        connLabel.setForeground(ACCENT_GOLD);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() {
                try {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            @Override protected void done() {
                try {
                    boolean ok = get();
                    connLabel.setText(ok ? "● Connected" : "● Disconnected");
                    connLabel.setForeground(ok ? ACCENT_GREEN : ACCENT_PINK);
                    setStatus(ok ? "Connected to FreelancerManagementDB" : "Connection failed — check DB_URL/credentials");
                } catch (Exception e) {
                    connLabel.setText("● Error");
                    connLabel.setForeground(ACCENT_PINK);
                }
            }
        };
        worker.execute();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (statusLabel != null) statusLabel.setText(msg);
        });
    }

    private Color blend(Color a, Color b, float t) {
        return new Color(
            (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t)
        );
    }

    private void animateIn() {
}

    // ── Main ───────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new NexusHub().setVisible(true);
        });
    }
}
