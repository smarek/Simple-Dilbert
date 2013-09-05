package com.lamerman;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Activity para escolha de arquivos/diretorios.
 *
 * @author android
 */
public class FileDialog extends SherlockListActivity {

    /**
     * Chave de um item da lista de paths.
     */
    private static final String ITEM_KEY = "key";

    /**
     * Imagem de um item da lista de paths (diretorio ou arquivo).
     */
    private static final String ITEM_IMAGE = "image";

    /**
     * Diretorio raiz.
     * <p/>
     * Update: Root must be external storage directory because of
     * DownloadManager
     */
    private static final String ROOT = Environment
            .getExternalStorageDirectory().toString();

    /**
     * Parametro de entrada da Activity: path inicial. Padrao: ROOT.
     */
    public static final String START_PATH = "START_PATH";

    /**
     * Parametro de entrada da Activity: filtro de formatos de arquivos. Padrao:
     * null.
     */
    private static final String FORMAT_FILTER = "FORMAT_FILTER";

    /**
     * Parametro de saida da Activity: path escolhido. Padrao: null.
     */
    public static final String RESULT_PATH = "RESULT_PATH";

    /**
     * Parametro de entrada da Activity: se e permitido escolher diretorios.
     * Padrao: falso.
     */
    public static final String CAN_SELECT_DIR = "CAN_SELECT_DIR";

    private List<String> path = null;
    private TextView myPath;
    private ArrayList<HashMap<String, Object>> mList;

    private Button selectButton;

    private String parentPath;
    private String currentPath = ROOT;

    private String[] formatFilter = null;

    private boolean canSelectDir = false;

    private File selectedFile;
    private final HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();

    private void addItem(String fileName, int imageId) {
        HashMap<String, Object> item = new HashMap<String, Object>();
        item.put(ITEM_KEY, fileName);
        item.put(ITEM_IMAGE, imageId);
        mList.add(item);
    }

    private void getDir(String dirPath) {

        boolean useAutoSelection = dirPath.length() < currentPath.length();

        Integer position = lastPositions.get(parentPath);

        getDirImpl(dirPath);

        if (position != null && useAutoSelection) {
            getListView().setSelection(position);
        }

    }

    /**
     * Monta a estrutura de arquivos e diretorios filhos do diretorio fornecido.
     *
     * @param dirPath Diretorio pai.
     */
    private void getDirImpl(final String dirPath) {

        currentPath = dirPath;

        path = new ArrayList<String>();
        mList = new ArrayList<HashMap<String, Object>>();

        File f = new File(currentPath);
        File[] files = f.listFiles();
        if (files == null) {
            currentPath = ROOT;
            f = new File(currentPath);
            files = f.listFiles();
        }
        myPath.setText(getText(R.string.location) + currentPath);

        if (!currentPath.equals(ROOT)) {

            addItem(ROOT, R.drawable.folder);
            path.add(ROOT);

            addItem("../", R.drawable.folder);
            path.add(f.getParent());
            parentPath = f.getParent();

        }

        NavigableMap<String, String> dirsMap = new TreeMap<String, String>();
        NavigableMap<String, String> dirsPathMap = new TreeMap<String, String>();
        NavigableMap<String, String> filesMap = new TreeMap<String, String>();
        NavigableMap<String, String> filesPathMap = new TreeMap<String, String>();
        if (files == null)
            files = new File[0];
        for (File file : files) {
            if (file.isDirectory()) {
                String dirName = file.getName();
                dirsMap.put(dirName, dirName);
                dirsPathMap.put(dirName, file.getPath());
            } else {
                final String fileName = file.getName();
                final String fileNameLwr = fileName
                        .toLowerCase(new Locale("en"));
                // se ha um filtro de formatos, utiliza-o
                if (formatFilter != null) {
                    boolean contains = false;
                    for (String aFormatFilter : formatFilter) {
                        final String formatLwr = aFormatFilter
                                .toLowerCase(new Locale("en"));
                        if (fileNameLwr.endsWith(formatLwr)) {
                            contains = true;
                            break;
                        }
                    }
                    if (contains) {
                        filesMap.put(fileName, fileName);
                        filesPathMap.put(fileName, file.getPath());
                    }
                    // senao, adiciona todos os arquivos
                } else {
                    filesMap.put(fileName, fileName);
                    filesPathMap.put(fileName, file.getPath());
                }
            }
        }
        path.addAll(dirsPathMap.tailMap("").values());
        path.addAll(filesPathMap.tailMap("").values());

        SimpleAdapter fileList = new SimpleAdapter(this, mList,
                R.layout.file_dialog_row,
                new String[]{ITEM_KEY, ITEM_IMAGE}, new int[]{
                R.id.fdrowtext, R.id.fdrowimage});

        for (String dir : dirsMap.tailMap("").values()) {
            addItem(dir, R.drawable.folder);
        }

        for (String file : filesMap.tailMap("").values()) {
            addItem(file, R.drawable.file);
        }

        fileList.notifyDataSetChanged();

        setListAdapter(fileList);

    }

    /**
     * Called when the activity is first created. Configura todos os parametros
     * de entrada e das VIEWS..
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_dialog_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myPath = (TextView) findViewById(R.id.fd_path);

        selectButton = (Button) findViewById(R.id.fd_select);
        selectButton.setEnabled(false);
        selectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (selectedFile != null) {
                    getIntent().putExtra(RESULT_PATH, selectedFile.getPath());
                    setResult(RESULT_OK, getIntent());
                    finish();
                }
            }
        });
        formatFilter = getIntent().getStringArrayExtra(FORMAT_FILTER);

        canSelectDir = getIntent().getBooleanExtra(CAN_SELECT_DIR, true);

        String startPath = getIntent().getStringExtra(START_PATH);
        startPath = startPath != null ? startPath : ROOT;
        if (canSelectDir) {
            selectedFile = new File(startPath);
            selectButton.setEnabled(true);
        }
        getDir(startPath);
    }

    /**
     * Quando clica no item da lista, deve-se: 1) Se for diretorio, abre seus
     * arquivos filhos; 2) Se puder escolher diretorio, define-o como sendo o
     * path escolhido. 3) Se for arquivo, define-o como path escolhido. 4) Ativa
     * botao de selecao.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        File file = new File(path.get(position));

        if (file.isDirectory()) {
            selectButton.setEnabled(false);
            if (file.canRead()) {
                lastPositions.put(currentPath, position);
                getDir(path.get(position));
                if (canSelectDir) {
                    selectedFile = file;
                    v.setSelected(true);
                    selectButton.setEnabled(true);
                }
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(
                                "[" + file.getName() + "] "
                                        + getText(R.string.cant_read_folder))
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                    }
                                }).show();
            }
        } else {
            selectedFile = file;
            v.setSelected(true);
            selectButton.setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
