package com.kineticdata.migrator.models;

import com.bmc.arsys.api.AttachmentValue;
import com.bmc.arsys.api.Entry;

public class Attachment {
    public static final String FORM = "KS_ACC_Attachment";
    public static final int ENTRY_ID = 1;
    public static final int ID = 179;
    public static final int FILE = 700000001;
    private final String id;
    private final String entryId;
    private final String fileName;
    private final long fileSize;

    public Attachment(Entry entry) {
        this.id = (String) entry.get(ID).getValue();
        this.entryId = (String) entry.get(ENTRY_ID).getValue();
        this.fileName = ((AttachmentValue) entry.get(FILE).getValue()).getValueFileName();
        this.fileSize = ((AttachmentValue) entry.get(FILE).getValue()).getOriginalSize();
    }

    public String getId() {
        return id;
    }

    public String getEntryId() {
        return entryId;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }
}