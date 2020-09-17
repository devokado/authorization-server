package com.idco.mesghal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String title;
    private String content;
    @CreationTimestamp
    private LocalDateTime cdt;
    @UpdateTimestamp
    private LocalDateTime udt;

    public long getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCdt() {
        return cdt;
    }

    public void setCdt(LocalDateTime cdt) {
        this.cdt = cdt;
    }

    public LocalDateTime getUdt() {
        return udt;
    }

    public void setUdt(LocalDateTime udt) {
        this.udt = udt;
    }


}
