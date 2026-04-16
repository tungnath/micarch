package com.micarch.ddd.order.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Shipping Address — Value Object (immutable, compared by value).
 * Belongs to the Order bounded context only.
 */
@Embeddable
public class ShippingAddress {

    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    protected ShippingAddress() {
    }

    public ShippingAddress(String street, String city, String state, String zipCode, String country) {
        if (street == null || street.isBlank()) throw new IllegalArgumentException("Street is required");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("City is required");
        if (zipCode == null || zipCode.isBlank()) throw new IllegalArgumentException("Zip code is required");
        this.street = street;
        this.city = city;
        this.state = state != null ? state : "";
        this.zipCode = zipCode;
        this.country = country != null ? country : "IN";
    }

    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
    public String getCountry() { return country; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShippingAddress that = (ShippingAddress) o;
        return street.equals(that.street) && city.equals(that.city) &&
               state.equals(that.state) && zipCode.equals(that.zipCode) &&
               country.equals(that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, zipCode, country);
    }

    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " " + zipCode + ", " + country;
    }
}
