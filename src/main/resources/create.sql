/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  altmf
 * Created: 27.11.2017
 */

CREATE USER IF NOT EXISTS LOCAL_ADMIN PASSWORD 123 ADMIN;

DROP SCHEMA IF EXISTS QUE;

CREATE SCHEMA IF NOT EXISTS QUE;

CREATE ROLE IF NOT EXISTS ALL_RIGHT;

GRANT ALL_RIGHT TO LOCAL_ADMIN;

GRANT ALTER ANY SCHEMA TO LOCAL_ADMIN;

SET SCHEMA QUE;

CREATE TABLE QUE.CLS_QUEST(
    ID              BIGINT IDENTITY,
    IS_DELETED      INT DEFAULT 0,
    QUEST_TEXT      CLOB
);

CREATE TABLE QUE.CLS_QUEST_PHOTO(
    ID              BIGINT IDENTITY,
    ID_QUEST        BIGINT NOT NULL,
    IS_DELETED      INT DEFAULT 0,
    REL_FILE_PATH   CLOB,
    PHOTO_TEXT      CLOB,
    FOREIGN KEY(ID_QUEST) REFERENCES CLS_QUEST(ID)
);

CREATE TABLE QUE.CLS_ANSWER(
    ID              BIGINT IDENTITY,
    ID_QUEST        BIGINT NOT NULL,
    IS_DELETED      INT DEFAULT 0,
    ANSWER_TEXT     CLOB,
    ANSWER_COMMENT  CLOB,
    FOREIGN KEY(ID_QUEST) REFERENCES CLS_QUEST(ID)
);

CREATE TABLE QUE.REG_QUEST_ANSWER(
    ID           BIGINT IDENTITY,
    ID_QUEST     BIGINT NOT NULL,
    ID_ANSWER    BIGINT NOT NULL,
    IS_DELETED   INT DEFAULT 0,
    DATE_ANSWER  TIMESTAMP,
    FOREIGN KEY(ID_QUEST) REFERENCES CLS_QUEST(ID),
    FOREIGN KEY(ID_ANSWER) REFERENCES CLS_ANSWER(ID)
);

CREATE VIEW VW_REG_LAST_QUEST_ANSWER AS (
    SELECT RQA.ID, RQA.ID_QUEST, RQA.ID_ANSWER 
    FROM (
        SELECT ID_QUEST, MAX(DATE_ANSWER) AS DATE_ANSWER
        FROM QUE.REG_QUEST_ANSWER
        GROUP BY
        ID_QUEST
    ) AS MAXANSW
    INNER JOIN QUE.REG_QUEST_ANSWER AS RQA
    ON MAXANSW.ID_QUEST = RQA.ID_QUEST AND MAXANSW.DATE_ANSWER = RQA.DATE_ANSWER
)