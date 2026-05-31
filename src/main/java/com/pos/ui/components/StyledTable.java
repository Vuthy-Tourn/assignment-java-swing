package com.pos.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class StyledTable extends JTable {

    public StyledTable(javax.swing.table.TableModel model) {
        super(model);
        styleTable();
    }

    private void styleTable() {
        setRowHeight(UIConstants.ROW_HEIGHT);
        setFont(UIConstants.FONT_BODY);
        setGridColor(UIConstants.BORDER);
        setShowVerticalLines(false);
        setIntercellSpacing(new Dimension(0, 1));
        setSelectionBackground(new Color(219, 234, 254));
        setSelectionForeground(UIConstants.TEXT_PRIMARY);
        setFillsViewportHeight(true);

        JTableHeader header = getTableHeader();
        header.setFont(UIConstants.FONT_HEADER);
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.BORDER));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        // Alternating row colors
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.BG_LIGHT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    public static JScrollPane inScrollPane(StyledTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }
}
