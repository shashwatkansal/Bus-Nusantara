package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.annotations.BelongsTo;
import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Order type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Orders")
public final class Order implements Model {
  public static final QueryField ID = field("Order", "id");
  public static final QueryField ROUTE = field("Order", "orderRouteId");
  public static final QueryField AGENT = field("Order", "orderAgentId");
  public static final QueryField PICKUP_LOCATION = field("Order", "pickupLocation");
  public static final QueryField PRICE = field("Order", "price");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="Route", isRequired = true) @BelongsTo(targetName = "orderRouteId", type = Route.class) Route route;
  private final @ModelField(targetType="Agent", isRequired = true) @BelongsTo(targetName = "orderAgentId", type = Agent.class) Agent agent;
  private final @ModelField(targetType="String", isRequired = true) String pickupLocation;
  private final @ModelField(targetType="Int", isRequired = true) Integer price;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String getId() {
      return id;
  }
  
  public Route getRoute() {
      return route;
  }
  
  public Agent getAgent() {
      return agent;
  }
  
  public String getPickupLocation() {
      return pickupLocation;
  }
  
  public Integer getPrice() {
      return price;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Order(String id, Route route, Agent agent, String pickupLocation, Integer price) {
    this.id = id;
    this.route = route;
    this.agent = agent;
    this.pickupLocation = pickupLocation;
    this.price = price;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Order order = (Order) obj;
      return ObjectsCompat.equals(getId(), order.getId()) &&
              ObjectsCompat.equals(getRoute(), order.getRoute()) &&
              ObjectsCompat.equals(getAgent(), order.getAgent()) &&
              ObjectsCompat.equals(getPickupLocation(), order.getPickupLocation()) &&
              ObjectsCompat.equals(getPrice(), order.getPrice()) &&
              ObjectsCompat.equals(getCreatedAt(), order.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), order.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getRoute())
      .append(getAgent())
      .append(getPickupLocation())
      .append(getPrice())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Order {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("route=" + String.valueOf(getRoute()) + ", ")
      .append("agent=" + String.valueOf(getAgent()) + ", ")
      .append("pickupLocation=" + String.valueOf(getPickupLocation()) + ", ")
      .append("price=" + String.valueOf(getPrice()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static RouteStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   * @throws IllegalArgumentException Checks that ID is in the proper format
   */
  public static Order justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Order(
      id,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      route,
      agent,
      pickupLocation,
      price);
  }
  public interface RouteStep {
    AgentStep route(Route route);
  }
  

  public interface AgentStep {
    PickupLocationStep agent(Agent agent);
  }
  

  public interface PickupLocationStep {
    PriceStep pickupLocation(String pickupLocation);
  }
  

  public interface PriceStep {
    BuildStep price(Integer price);
  }
  

  public interface BuildStep {
    Order build();
    BuildStep id(String id) throws IllegalArgumentException;
  }
  

  public static class Builder implements RouteStep, AgentStep, PickupLocationStep, PriceStep, BuildStep {
    private String id;
    private Route route;
    private Agent agent;
    private String pickupLocation;
    private Integer price;
    @Override
     public Order build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Order(
          id,
          route,
          agent,
          pickupLocation,
          price);
    }
    
    @Override
     public AgentStep route(Route route) {
        Objects.requireNonNull(route);
        this.route = route;
        return this;
    }
    
    @Override
     public PickupLocationStep agent(Agent agent) {
        Objects.requireNonNull(agent);
        this.agent = agent;
        return this;
    }
    
    @Override
     public PriceStep pickupLocation(String pickupLocation) {
        Objects.requireNonNull(pickupLocation);
        this.pickupLocation = pickupLocation;
        return this;
    }
    
    @Override
     public BuildStep price(Integer price) {
        Objects.requireNonNull(price);
        this.price = price;
        return this;
    }
    
    /** 
     * WARNING: Do not set ID when creating a new object. Leave this blank and one will be auto generated for you.
     * This should only be set when referring to an already existing object.
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     * @throws IllegalArgumentException Checks that ID is in the proper format
     */
    public BuildStep id(String id) throws IllegalArgumentException {
        this.id = id;
        
        try {
            UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
        } catch (Exception exception) {
          throw new IllegalArgumentException("Model IDs must be unique in the format of UUID.",
                    exception);
        }
        
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, Route route, Agent agent, String pickupLocation, Integer price) {
      super.id(id);
      super.route(route)
        .agent(agent)
        .pickupLocation(pickupLocation)
        .price(price);
    }
    
    @Override
     public CopyOfBuilder route(Route route) {
      return (CopyOfBuilder) super.route(route);
    }
    
    @Override
     public CopyOfBuilder agent(Agent agent) {
      return (CopyOfBuilder) super.agent(agent);
    }
    
    @Override
     public CopyOfBuilder pickupLocation(String pickupLocation) {
      return (CopyOfBuilder) super.pickupLocation(pickupLocation);
    }
    
    @Override
     public CopyOfBuilder price(Integer price) {
      return (CopyOfBuilder) super.price(price);
    }
  }
  
}
