class RemoveAuthSecretFromUsers < ActiveRecord::Migration
  def change
    remove_column :users, :auth_secret, :string
  end
end
