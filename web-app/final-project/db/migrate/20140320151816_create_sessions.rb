class CreateSessions < ActiveRecord::Migration
  def change
    create_table :sessions do |t|
      t.string :session_key
      t.string :auth_token
      t.references :user

      t.timestamps
    end
    add_index :sessions, :session_key
  end
end
