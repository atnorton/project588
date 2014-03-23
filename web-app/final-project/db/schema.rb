# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20140323175251) do

  create_table "sessions", force: true do |t|
    t.string   "session_key"
    t.string   "auth_token"
    t.integer  "user_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.boolean  "logged_in"
  end

  add_index "sessions", ["session_key"], name: "index_sessions_on_session_key"

  create_table "users", force: true do |t|
    t.string   "name"
    t.string   "email"
    t.string   "session_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "auth_secret"
  end

end
