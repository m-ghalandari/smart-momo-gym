package de.momogym.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "EXERCISE_CATALOG")
public class Exercise {

    @Id
    @SequenceGenerator(
            name = "exercise_seq_gen",
            sequenceName = "EXERCISE_SEQ",
            initialValue = 1,
            allocationSize = 50
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "exercise_seq_gen"
    )
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @Column(name = "TARGET_MUSCLE")
    private String targetMuscle;

    @Column(name = "GIF_URL")
    private String gifUrl;

    @Column(name = "VIDEO_LINK")
    private String videoLink;

    public Exercise() {
    }

    public Exercise(String name, String targetMuscle, String gifUrl, String videoLink){
        this.name = name;
        this.targetMuscle = targetMuscle;
        this.gifUrl = gifUrl;
        this.videoLink = videoLink;
    }


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetMuscle() {
        return targetMuscle;
    }

    public void setTargetMuscle(String targetMuscle) {
        this.targetMuscle = targetMuscle;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }
}
