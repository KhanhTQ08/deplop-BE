package com.datn.demo.a;

public class SeatStatus {
	 private String seat;
	    private boolean available;

	    public SeatStatus(String seat, boolean available) {
	        this.seat = seat;
	        this.available = available;
	    }

	    // Getter và Setter
	    public String getSeat() {
	        return seat;
	    }

	    public void setSeat(String seat) {
	        this.seat = seat;
	    }

	    public boolean isAvailable() {
	        return available;
	    }

	    public void setAvailable(boolean available) {
	        this.available = available;
	    }
}
