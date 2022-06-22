package com.cst2335.finalproject;

/**
 * @Author Jordan Passant
 * @Since 2021-04-08
 *
 *  This class is for the instantiation of Car objects, used by
 *  classes/activities such as 'CarTraderModelDetailsActivity'
 */

public class CarTraderCarActivity {

    protected String make, model;
    protected long id;

    /**
     * Default constructor with a default value for the id
     */
    public CarTraderCarActivity(String u, String m)
    {
        this(u, m, 0);
    }

    /**
     * Constructor for 'Car' objects
     */
    public CarTraderCarActivity(String u, String m, long i)
    {
        this.make = u;
        this.model = m;
        this.id = i;
    }

    /**
     * Updates a car object's make/model
     * @param u Make
     * @param m Model
     */
    public void update(String u, String m) {
        this.make = u;
        this.model = m;
    }

    /**
     *  Getter for make of the car
     *  @return The car's make
     */
    public String getMake() {
        return make;
    }

    /**
     * Getter for the model of the car
     * @return The car's model
     */
    public String getModel()
    {
        return model;
    }

    /**
     * Getter for the car object's given DB id
     * @return
     */
    public long getId()
    {
        return id;
    }


}