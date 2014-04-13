class CreateStolenSessions < ActiveRecord::Migration
  def change
    create_table :stolen_sessions do |t|

      t.timestamps
    end
  end
end
