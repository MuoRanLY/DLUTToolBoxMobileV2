package com.Shirai_Kuroko.DLUTMobile.Entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class IDPhotoResult {

    @JsonProperty("data")
    private DataDTO data;
    @JsonProperty("ret")
    private Integer ret;
    @JsonProperty("errcode")
    private Integer errcode;
    @JsonProperty("errmsg")
    private String errmsg;

    public Integer getRet() {
        return ret;
    }

    public DataDTO getData() {
        return data;
    }

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("msg_id")
        private List<?> msg_id;
        @JsonProperty("conver_msg_id")
        private List<?> conver_msg_id;
        @JsonProperty("app_msg_id")
        private List<?> app_msg_id;
        @JsonProperty("id_photo")
        private String id_photo;

        public List<?> getApp_msg_id() {
            return app_msg_id;
        }

        public List<?> getConver_msg_id() {
            return conver_msg_id;
        }

        public List<?> getMsg_id() {
            return msg_id;
        }

        public String getId_photo() {
            return id_photo;
        }
    }
}
