package cn.ivhik.wyb.intellij.plugin.config;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.EditableModel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 插件设置页面的表格对象
 *
 * @author maomao
 * @version 1.3
 * @since 2019-05-05
 */
public class PluginSettingTable extends JBTable {
    private static final Logger LOG = Logger.getInstance(PluginSettingTable.class);

    /**
     * 序号列
     */
    private static final int ORDER_COLUMN = 0;

    /**
     * 图片 URL 列
     */
    private static final int URL_COLUMN = 1;

    public PluginSettingTable(List<String> defaultImageUrlList) {
        super(new ModelAdapter(defaultImageUrlList));
        super.setStriped(true);

        final TableColumnModel columnModel = getColumnModel();
        final TableColumn orderColumn = columnModel.getColumn(ORDER_COLUMN);
        orderColumn.setPreferredWidth(20);

        final TableColumn urlColumn = columnModel.getColumn(URL_COLUMN);
        urlColumn.setPreferredWidth(280);
    }

    /**
     * 获取表格的 {@link javax.swing.table.TableModel}
     *
     * <p>实现 {@link com.intellij.util.ui.EditableModel} 表示表格是可编辑的</p>
     *
     * @see com.intellij.ui.ToolbarDecorator#createDecorator(JTable)
     */
    @Override
    public ModelAdapter getModel() {
        return (ModelAdapter) super.getModel();
    }

    /**
     * 重置表格数据
     */
    public void resetTableList() {
        this.getModel().setRowsData(DefaultConfig.REMIND_IMAGE_LIST);
        LOG.info("reset image url list to default");
    }

    /**
     * 获取表格数据
     */
    public List<String> getTableList() {
        List<String> imageList = this.getModel().getRowsData();
        LOG.info("get image url list: " + imageList);
        return imageList;
    }

    /**
     * 设置表格数据
     */
    public void setTableList(List<String> imageList) {
        this.getModel().setRowsData(imageList);
        LOG.info("set image url list to: " + imageList);
    }

    /**
     * 插件设置页面的表格对象的 {@link javax.swing.table.TableModel}
     *
     * <p>实现 {@link com.intellij.util.ui.EditableModel} 表示表格是可编辑的</p>
     */
    private static class ModelAdapter extends AbstractTableModel implements EditableModel {
        private static final Logger LOG = Logger.getInstance(ModelAdapter.class);

        /**
         * 图片 URL 列表数据
         */
        private final List<String> imageUrlList;

        public ModelAdapter(List<String> defaultImageUrlList) {
            // 复制表格数据（使用深拷贝模式），避免修改默认图片配置
            this.imageUrlList = new ArrayList<>(defaultImageUrlList.size());
            this.imageUrlList.addAll(defaultImageUrlList);
        }

        /**
         * 获取总行数
         */
        @Override
        public int getRowCount() {
            return imageUrlList.size();
        }

        /**
         * 获取总列数
         */
        @Override
        public int getColumnCount() {
            return 2;
        }

        /**
         * 根据列号，获取默认列名
         */
        @Override
        public String getColumnName(int column) {
            return column == ORDER_COLUMN ? "序号" : "图片 URL";
        }

        /**
         * 设置单元格对象的 {@link java.lang.Class}
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        /**
         * 是否可以编辑单元格内容
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        /**
         * 根据单元格坐标，获取单元格内容
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return columnIndex == ORDER_COLUMN ? String.valueOf(rowIndex) : imageUrlList.get(rowIndex);
        }

        /**
         * 添加表格行
         */
        @Override
        public void addRow() {
            FileChooserDescriptor descriptor = PluginSettingConfig.IMAGE_FILE_CHOOSER;
            FileChooserDialog dialog = FileChooserFactory.getInstance().createFileChooser(descriptor, null, null);
            VirtualFile[] files = dialog.choose(null);
            List<String> chosenImageUrlList = Stream.of(files)
                    .map(imageFile -> {
                        try {
                            return VfsUtil.toUri(imageFile).toURL().toString();
                        } catch (MalformedURLException e) {
                            LOG.error("parse the image \"" + imageFile.getName() + "\" to URL error", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(imageUrl -> !imageUrlList.contains(imageUrl))
                    .collect(Collectors.toList());
            if (chosenImageUrlList.size() != 0) {
                imageUrlList.addAll(chosenImageUrlList);
                LOG.info("add rows: " + chosenImageUrlList);
                super.fireTableRowsInserted(imageUrlList.size() - 1 - files.length, imageUrlList.size() - 1);
            } else {
                LOG.info("choose no files");
            }
        }

        /**
         * 交换表格行
         */
        @Override
        public void exchangeRows(int oldIndex, int newIndex) {
            final String oldImgUrl = imageUrlList.get(oldIndex);
            final String newImgUrl = imageUrlList.get(newIndex);
            imageUrlList.set(oldIndex, newImgUrl);
            imageUrlList.set(newIndex, oldImgUrl);
            LOG.info(String.format("exchange rows index: %d -> %d", oldIndex, newIndex));
            super.fireTableRowsUpdated(Math.min(oldIndex, newIndex), Math.max(oldIndex, newIndex));
        }

        /**
         * 是否可以交换表格行
         */
        @Override
        public boolean canExchangeRows(int oldIndex, int newIndex) {
            return true;
        }

        /**
         * 删除表格行
         */
        @Override
        public void removeRow(int idx) {
            imageUrlList.remove(idx);
            LOG.info("remove row index: " + idx);
            super.fireTableRowsDeleted(idx, idx);
        }

        /**
         * 获取表格行数据
         */
        public List<String> getRowsData() {
            List<String> rows = Collections.unmodifiableList(new ArrayList<>(imageUrlList));
            LOG.info("get rows data: " + rows);
            return rows;
        }

        /**
         * 设置表格行数据
         */
        public void setRowsData(List<String> list) {
            imageUrlList.clear();
            imageUrlList.addAll(list);
            LOG.info("set rows data: " + list);
            super.fireTableDataChanged();
        }
    }
}
