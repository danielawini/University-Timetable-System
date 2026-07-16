package com.university.models;

public class Room {private int roomId;
    private String roomName;
    private int capacity;
    private String facilities;

    public Room() {}

    public Room(int roomId, String roomName, int capacity, String facilities) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.capacity = capacity;
        this.facilities = facilities;
    }

    // Getters and Setters
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }
}
