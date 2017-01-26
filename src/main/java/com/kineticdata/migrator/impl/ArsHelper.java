package com.kineticdata.migrator.impl;

import com.bmc.arsys.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArsHelper {

    public static Entry getOneEntry(ARServerUser user, String form, String qual, int[] fields) {
        List<Entry> entries = getEntries(user, form, parseQual(user, form, qual), 0, 2,
                Collections.EMPTY_LIST, fields);
        if (entries.size() > 1)
            throw new RuntimeException("Query returned multiple results when a single was expected");
        return entries.isEmpty() ? null : entries.get(0);
    }

    public static List<Entry> getAllEntries(ARServerUser user, String form, String qual,
                                            int queryLimit, List<SortInfo> order, int[] fields) {
        return getAllEntries(user, form, parseQual(user, form, qual), queryLimit, order, fields);
    }

    public static List<Entry> getAllEntries(ARServerUser user, String form, QualifierInfo qual,
                                            int queryLimit, List<SortInfo> order, int[] fields) {
        List<Entry> result = new ArrayList<>();
        List<Entry> current;
        do {
            current = getEntries(user, form, qual, result.size(), queryLimit, order, fields);
            result.addAll(current);
        } while (!current.isEmpty());
        return result;
    }

    public static List<Entry> getEntries(ARServerUser user, String form, QualifierInfo qual,
                                         int offset, int limit, List<SortInfo> order, int[] fields) {
        try {
            // if the qualification is null do not do an open query, instead return an empty result
            return qual == null ? Collections.EMPTY_LIST
                    : user.getListEntryObjects(form, qual, offset, limit, order, fields, false, null);
        } catch (ARException e) {
            throw new RuntimeException(e);
        }
    }

    public static QualifierInfo parseQual(ARServerUser user, String form, String qual) {
        try {
            QualifierInfo result = user.parseQualification(form, qual);
            if (result == null) {
                throw new RuntimeException(String.format("Got null parsing the following qualification for '%s': %s", form, qual));
            }
            return result;
        } catch (ARException e) {
            throw new RuntimeException(e);
        }
    }

    public static QualifierInfo buildQual(int fieldId, List<String> values) {
        // initialize the result to null, if the initial call was made with an empty list we return
        // the null value, note that recursive calls should never be made with an empty list
        QualifierInfo result = null;
        if (!values.isEmpty()) {
            // build the qualifier info for the string value at the head of the list
            String head = values.get(0);
            QualifierInfo qualInfo =
                    new QualifierInfo(
                            new RelationalOperationInfo(
                                    RelationalOperationInfo.AR_REL_OP_EQUAL,
                                    new ArithmeticOrRelationalOperand(OperandType.FIELDID, fieldId),
                                    new ArithmeticOrRelationalOperand(new Value(head))));
            // if there is no tail we return the qual info above, otherwise we make a recursive call
            // that joins the qual info above with an or and the recursive result
            List<String> tail = values.subList(1, values.size());
            result = tail.isEmpty()
                    ? qualInfo
                    : new QualifierInfo(QualifierInfo.AR_COND_OP_OR, buildQual(fieldId, tail), qualInfo);
        }
        return result;
    }

    public static ARServerUser createUser(Config config) {
        return new ARServerUser(config.getReUsername(), config.getRePassword(), "",
                config.getReServer(), config.getRePort());
    }
}